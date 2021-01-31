package ru.csdm.stats.webapp.view;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.webapp.PojoStatus;
import ru.csdm.stats.webapp.Row;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.Constants.PROJECT_DATABASE_SERVER_TIMEZONES;
import static ru.csdm.stats.common.model.collector.tables.DriverProperty.DRIVER_PROPERTY;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;
import static ru.csdm.stats.common.model.csstats.Tables.HISTORY;
import static ru.csdm.stats.common.model.csstats.Tables.RANK;
import static ru.csdm.stats.common.model.csstats.tables.Player.PLAYER;
import static ru.csdm.stats.common.model.csstats.tables.PlayerIp.PLAYER_IP;
import static ru.csdm.stats.common.model.csstats.tables.PlayerSteamid.PLAYER_STEAMID;
import static ru.csdm.stats.common.utils.SomeUtils.buildHikariDataSource;
import static ru.csdm.stats.common.utils.SomeUtils.configJooqContext;
import static ru.csdm.stats.webapp.PojoStatus.*;

@ViewScoped
@Named
@Slf4j
public class ViewProject {
    @Autowired
    private DSLContext collectorDsl;

    @Getter
    private Project selectedProject;
    @Getter
    private List<Row<DriverProperty>> currentProjectDriverPropertyRows;

    @Getter
    private SelectItem[] availableTimeZones;

    @Getter
    private boolean connectionValidated;
    @Getter
    private boolean addServerBtnDisabled;

    private Integer tablesCount;

    @PostConstruct
    public void init() {
        availableTimeZones = Stream.of(PROJECT_DATABASE_SERVER_TIMEZONES)
                .map(timezone -> new SelectItem(timezone, timezone.getLiteral()))
                .toArray(SelectItem[]::new);
    }

    public void fetch() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String projectId = request.getParameter("projectId");

        FacesContext fc = FacesContext.getCurrentInstance();

        if (!StringUtils.isNumeric(projectId)) {
            fc.addMessage(null, new FacesMessage(SEVERITY_WARN, "Invalid projectId", ""));
            return;
        }

        selectedProject = collectorDsl.selectFrom(PROJECT)
                .where(PROJECT.ID.eq(UInteger.valueOf(projectId)))
                .fetchOneInto(Project.class);

        if(Objects.isNull(selectedProject)) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage(null, new FacesMessage(SEVERITY_WARN, "Project [" + projectId + "] not founded", ""));
            return;
        }

        fetchDriverProperties();
    }

    public void fetchDriverProperties() {
        if(Objects.isNull(selectedProject))
            return;

        currentProjectDriverPropertyRows = collectorDsl.selectFrom(DRIVER_PROPERTY)
                .where(DRIVER_PROPERTY.PROJECT_ID.eq(selectedProject.getId()))
                .orderBy(DRIVER_PROPERTY.ID.asc())
                .fetchInto(DriverProperty.class)
                .stream()
                .map(driverProperty -> new Row<>(driverProperty, EXISTED))
                .collect(Collectors.toList());

        addServerBtnDisabled = false;
    }

    public void validate() {
        connectionValidated = false;

        FacesContext fc = FacesContext.getCurrentInstance();

        try(HikariDataSource hds = buildHikariDataSource(selectedProject.getDatabaseSchema() + "-connection-project-#" + selectedProject.getId())) {
            hds.setJdbcUrl("jdbc:mysql://" + selectedProject.getDatabaseHostport() + "/" + selectedProject.getDatabaseSchema());
            hds.setSchema(selectedProject.getDatabaseSchema());
            hds.setUsername(selectedProject.getDatabaseUsername());
            hds.setPassword(selectedProject.getDatabasePassword());
            hds.addDataSourceProperty("serverTimezone", selectedProject.getDatabaseServerTimezone().getLiteral());

            for (Iterator<Row<DriverProperty>> iterator = currentProjectDriverPropertyRows.iterator(); iterator.hasNext(); ) {
                Row<DriverProperty> row = iterator.next();

                DriverProperty driverProperty = row.getPojo();
                PojoStatus pojoStatus = row.getStatus();

                if(pojoStatus == TO_REMOVE && Objects.isNull(driverProperty.getId())) {
                    iterator.remove();
                    continue;
                }

                if (StringUtils.isBlank(driverProperty.getKey())) {
                    if(Objects.isNull(driverProperty.getId()))
                        iterator.remove();
                    else
                        row.setStatus(TO_REMOVE);
                } else if(pojoStatus != TO_REMOVE) {
                    if (StringUtils.isBlank(driverProperty.getValue()))
                        driverProperty.setValue("");

                    if(log.isDebugEnabled())
                        log.debug("\naddDataSourceProperty " + driverProperty.getKey() + "=" + driverProperty.getValue());

                    hds.addDataSourceProperty(driverProperty.getKey(), driverProperty.getValue());
                }
            }

            /* Override settings from com.zaxxer.hikari.HikariConfig */
            hds.setMaximumPoolSize(2);
            hds.setMinimumIdle(1);

            hds.setConnectionTimeout(SECONDS.toMillis(10));
            hds.setValidationTimeout(SECONDS.toMillis(5));
            hds.setIdleTimeout(SECONDS.toMillis(29));
            hds.setMaxLifetime(SECONDS.toMillis(30));

            DSLContext statsDsl = configJooqContext(hds, SQLDialect.MYSQL, selectedProject.getDatabaseSchema(), 10);

            statsDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                tablesCount = transactionalDsl.selectCount()
                        .from(DSL.table("information_schema.TABLES"))
                        .where(DSL.field("TABLE_SCHEMA").eq(selectedProject.getDatabaseSchema()),
                                DSL.field("TABLE_NAME").in(
                                    HISTORY.getName(),
                                    PLAYER.getName(),
                                    PLAYER_IP.getName(),
                                    PLAYER_STEAMID.getName(),
                                    RANK.getName()))
                        .fetchOne(DSL.count());
            });
        } catch (Exception e) {
            tablesCount = null;

            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed validation project [" + selectedProject.getId() + "]",
                    e.toString()));

            return;
        } finally {
            addServerBtnDisabled = false;
        }

        if(tablesCount != 5) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed validation project [" + selectedProject.getId() + "]",
                    "One of 5 database tables is missing"));

            return;
        }

        fc.addMessage("msgs", new FacesMessage("Project [" + selectedProject.getId() + "] validated", ""));
        connectionValidated = true;
    }

    public void save() {
        FacesContext fc = FacesContext.getCurrentInstance();

        try {
            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                transactionalDsl.update(PROJECT)
                        .set(PROJECT.NAME, selectedProject.getName())
                        .set(PROJECT.DESCRIPTION, StringUtils.isBlank(selectedProject.getDescription()) ? null : selectedProject.getDescription())
                        .set(PROJECT.DATABASE_HOSTPORT, selectedProject.getDatabaseHostport())
                        .set(PROJECT.DATABASE_SCHEMA, selectedProject.getDatabaseSchema())
                        .set(PROJECT.DATABASE_PASSWORD, selectedProject.getDatabasePassword())
                        .set(PROJECT.DATABASE_SERVER_TIMEZONE, selectedProject.getDatabaseServerTimezone())
                        .where(PROJECT.ID.eq(selectedProject.getId()))
                        .execute();

                List<UInteger> toRemoveDriverPropertyIds = new ArrayList<>(currentProjectDriverPropertyRows.size());
                for (Iterator<Row<DriverProperty>> iterator = currentProjectDriverPropertyRows.iterator(); iterator.hasNext(); ) {
                    Row<DriverProperty> row = iterator.next();

                    if(row.getStatus() == TO_REMOVE) {
                        toRemoveDriverPropertyIds.add(row.getPojo().getId());
                        iterator.remove();
                    }
                }

                if(!toRemoveDriverPropertyIds.isEmpty()) {
                    transactionalDsl.deleteFrom(DRIVER_PROPERTY)
                            .where(DRIVER_PROPERTY.ID.in(toRemoveDriverPropertyIds))
                            .execute();
                }

                for (Row<DriverProperty> row : currentProjectDriverPropertyRows) {
                    DriverProperty driverProperty = row.getPojo();
                    PojoStatus pojoStatus = row.getStatus();

                    if(pojoStatus == CHANGED) {
                        transactionalDsl.update(DRIVER_PROPERTY)
                                .set(DRIVER_PROPERTY.KEY, driverProperty.getKey())
                                .set(DRIVER_PROPERTY.VALUE, driverProperty.getValue())
                                .where(DRIVER_PROPERTY.ID.eq(driverProperty.getId()))
                                .execute();
                    } else if(pojoStatus == NEW) {
                        transactionalDsl.insertInto(DRIVER_PROPERTY)
                                .set(DRIVER_PROPERTY.KEY, driverProperty.getKey())
                                .set(DRIVER_PROPERTY.VALUE, driverProperty.getValue())
                                .set(DRIVER_PROPERTY.PROJECT_ID, driverProperty.getProjectId())
                                .execute();
                    }
                }

                currentProjectDriverPropertyRows.clear();
            });

            fetchDriverProperties();

            fc.addMessage("msgs", new FacesMessage("Project [" + selectedProject.getId() + "] saved", ""));
        } catch (Exception e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save project [" + selectedProject.getId() + "]",
                    e.toString()));
        } finally {
            connectionValidated = false;
        }
    }

    public void onRowEdit(RowEditEvent event) {
        Row<DriverProperty> row = (Row<DriverProperty>) event.getObject();

        if(Objects.nonNull(row.getPojo().getId())) {
            row.setStatus(CHANGED);
            row.setPreviousStatus(null);
        } else if(currentProjectDriverPropertyRows.get(currentProjectDriverPropertyRows.size() -1).equals(row)) {
            addServerBtnDisabled = false;
        }

        if(log.isDebugEnabled())
            log.debug("\nonRowEdit " + row);
    }

    public void onAddProperty() {
        if(log.isDebugEnabled())
            log.debug("\nonAddProperty");

        connectionValidated = false;

        DriverProperty driverProperty = new DriverProperty();
        driverProperty.setProjectId(selectedProject.getId());
        currentProjectDriverPropertyRows.add(new Row<>(driverProperty, NEW));
        addServerBtnDisabled = true;
    }

    public void onRestoreProperty(Row<DriverProperty> row) {
        connectionValidated = false;

        row.setStatus(row.getPreviousStatus());
        row.setPreviousStatus(null);

        if(log.isDebugEnabled())
            log.debug("\nonRestoreProperty " + row);
    }

    public void onRemoveProperty(Row<DriverProperty> row) {
        connectionValidated = false;

        row.setPreviousStatus(row.getStatus());
        row.setStatus(TO_REMOVE);

        if(log.isDebugEnabled())
            log.debug("\nonRemoveProperty " + row);
    }
}