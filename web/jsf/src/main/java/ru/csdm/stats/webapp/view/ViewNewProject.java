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
import ru.csdm.stats.common.Constants;
import ru.csdm.stats.common.model.collector.enums.ProjectDatabaseServerTimezone;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.common.utils.SomeUtils;
import ru.csdm.stats.webapp.DependentUtil;
import ru.csdm.stats.webapp.PojoStatus;
import ru.csdm.stats.webapp.Row;
import ru.csdm.stats.webapp.application.ChangesCounter;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
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
public class ViewNewProject {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private ChangesCounter changesCounter;

    @Getter
    private Project selectedProject;
    @Getter
    private List<Row<DriverProperty>> driverPropertyRows;

    @Getter
    private boolean connectionValidated;
    @Getter
    private boolean addDriverPropertyBtnDisabled;

    private Integer tablesCount;
    private int localChangesCounter;

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        selectedProject = new Project();
        selectedProject.setRegDatetime(LocalDateTime.now());

        String defaultTimeZoneStr = TimeZone.getDefault().getID();
        ProjectDatabaseServerTimezone detectedTimeZone = SomeUtils.timezoneEnumByLiteral
                .getOrDefault(defaultTimeZoneStr, null);
        selectedProject.setDatabaseServerTimezone(detectedTimeZone);

        driverPropertyRows = new ArrayList<>();
    }

    public void validate() {
        connectionValidated = false;

        FacesContext fc = FacesContext.getCurrentInstance();

        try(HikariDataSource hds = buildHikariDataSource(selectedProject.getDatabaseSchema()
                + "-connection-project-new")) {
            hds.setJdbcUrl("jdbc:mysql://" + selectedProject.getDatabaseHostport() + "/" + selectedProject.getDatabaseSchema());
            hds.setSchema(selectedProject.getDatabaseSchema());
            hds.setUsername(selectedProject.getDatabaseUsername());
            hds.setPassword(selectedProject.getDatabasePassword());
            hds.addDataSourceProperty("serverTimezone", selectedProject.getDatabaseServerTimezone().getLiteral());

            for (Iterator<Row<DriverProperty>> iterator = driverPropertyRows.iterator(); iterator.hasNext(); ) {
                Row<DriverProperty> row = iterator.next();

                DriverProperty driverProperty = row.getPojo();
                PojoStatus pojoStatus = row.getStatus();

                if(pojoStatus == TO_REMOVE) {
                    iterator.remove();
                    continue;
                }

                if (StringUtils.isBlank(driverProperty.getKey())) {
                    iterator.remove();
                    continue;
                }

                if (StringUtils.isBlank(driverProperty.getValue()))
                    driverProperty.setValue("");

                if (log.isDebugEnabled())
                    log.debug("\naddDataSourceProperty " + driverProperty.getKey() + "=" + driverProperty.getValue());

                hds.addDataSourceProperty(driverProperty.getKey(), driverProperty.getValue());
            }

            /* Override settings from com.zaxxer.hikari.HikariConfig */
            hds.setMaximumPoolSize(2);
            hds.setMinimumIdle(1);

            hds.setConnectionTimeout(SECONDS.toMillis(10));
            hds.setValidationTimeout(SECONDS.toMillis(5));
            hds.setIdleTimeout(SECONDS.toMillis(29));
            hds.setMaxLifetime(SECONDS.toMillis(30));

            log.info("Using datasource settings: jdbcUrl=" + hds.getJdbcUrl()
                    + ", schema=" + hds.getSchema()
                    + ", dataSourceProperties=" + hds.getDataSourceProperties());

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
                    "Failed validation new project", e.toString()));

            return;
        } finally {
            addDriverPropertyBtnDisabled = false;
        }

        if(tablesCount != 5) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed validation new project",
                    "One of 5 database tables is missing"));

            return;
        }

        fc.addMessage("msgs", new FacesMessage("Project settings validated", ""));
        connectionValidated = true;
    }

    public String save() {
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;

        try {
            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                UInteger projectId = transactionalDsl.insertInto(PROJECT)
                        .set(PROJECT.NAME, selectedProject.getName())
                        .set(PROJECT.DESCRIPTION, StringUtils.isBlank(selectedProject.getDescription()) ? null : selectedProject.getDescription())
                        .set(PROJECT.DATABASE_HOSTPORT, selectedProject.getDatabaseHostport())
                        .set(PROJECT.DATABASE_SCHEMA, selectedProject.getDatabaseSchema())
                        .set(PROJECT.DATABASE_USERNAME, selectedProject.getDatabaseUsername())
                        .set(PROJECT.DATABASE_PASSWORD, selectedProject.getDatabasePassword())
                        .set(PROJECT.DATABASE_SERVER_TIMEZONE, selectedProject.getDatabaseServerTimezone())
                        .returning(PROJECT.ID)
                        .fetchOne().getId();

                ++localChangesCounter;

                selectedProject.setId(projectId);

                for (Row<DriverProperty> row : driverPropertyRows) {
                    DriverProperty driverProperty = row.getPojo();

                    localChangesCounter += transactionalDsl.insertInto(DRIVER_PROPERTY)
                            .set(DRIVER_PROPERTY.KEY, driverProperty.getKey())
                            .set(DRIVER_PROPERTY.VALUE, driverProperty.getValue())
                            .set(DRIVER_PROPERTY.PROJECT_ID, selectedProject.getId())
                            .execute();
                }
            });

            changesCounter.increment(localChangesCounter);

            return "/editProject?faces-redirect=true&projectId=" + selectedProject.getId();
        } catch (Exception e) {
            selectedProject.setId(null);
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save new project", e.toString()));

            return null;
        } finally {
            connectionValidated = false;
            addDriverPropertyBtnDisabled = false;
        }
    }

    public void onRowEdit(RowEditEvent event) {
        connectionValidated = false;

        Row<DriverProperty> row = (Row<DriverProperty>) event.getObject();

        if(driverPropertyRows.get(driverPropertyRows.size() -1).equals(row)) {
            addDriverPropertyBtnDisabled = false;
        }

        if(log.isDebugEnabled())
            log.debug("\nonRowEdit " + row);
    }

    public void onAddProperty() {
        if(log.isDebugEnabled())
            log.debug("\nonAddProperty");

        connectionValidated = false;

        DriverProperty driverProperty = new DriverProperty();
        driverPropertyRows.add(new Row<>(driverProperty, NEW));
        addDriverPropertyBtnDisabled = true;
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