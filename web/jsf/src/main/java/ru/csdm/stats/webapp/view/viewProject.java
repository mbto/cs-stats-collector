package ru.csdm.stats.webapp.view;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.webapp.PojoStatus;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
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
public class ViewProject {
    @Autowired
    private DSLContext collectorDsl;

    @Getter
    private boolean connectionValidated;

    @Getter
    private Project project;
    @Getter
    private List<MutableTriple<DriverProperty, UUID, PojoStatus>> driverPropertiesWithStatus = new ArrayList<>();

    @Getter
    private SelectItem[] availableTimeZones;

    @Getter
    private Integer tablesCount;

    @PostConstruct
    public void init() {
        availableTimeZones = Stream.of(PROJECT_DATABASE_SERVER_TIMEZONES)
                .map(timezone -> new SelectItem(timezone, timezone.getLiteral()))
                .toArray(SelectItem[]::new);
    }

    public void fetch() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String id = request.getParameter("id");

        FacesContext fc = FacesContext.getCurrentInstance();

        if (!StringUtils.isNumeric(id)) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, "Invalid id", ""));
            return;
        }

        project = collectorDsl.selectFrom(PROJECT)
                .where(PROJECT.ID.eq(UInteger.valueOf(id)))
                .fetchOneInto(Project.class);

        if(Objects.isNull(project)) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage("msgs", new FacesMessage("Project [" + id + "] not founded", ""));
            return;
        }

        fetchDriverProperties();
    }

    public void fetchDriverProperties() {
        if(Objects.isNull(project))
            return;

        collectorDsl.selectFrom(DRIVER_PROPERTY)
                .where(DRIVER_PROPERTY.PROJECT_ID.eq(project.getId()))
                .orderBy(DRIVER_PROPERTY.ID.asc())
                .fetchInto(DriverProperty.class)
                .forEach(driverProperty -> {
                    driverPropertiesWithStatus.add(MutableTriple.of(driverProperty, UUID.randomUUID(), EXISTED));
                });
    }

    public boolean validate() {
        connectionValidated = false;

        FacesContext fc = FacesContext.getCurrentInstance();

        try(HikariDataSource hds = buildHikariDataSource(project.getDatabaseSchema() + "-connection-project-#" + project.getId())) {
            hds.setJdbcUrl("jdbc:mysql://" + project.getDatabaseHostport() + "/" + project.getDatabaseSchema());
            hds.setSchema(project.getDatabaseSchema());
            hds.setUsername(project.getDatabaseUsername());
            hds.setPassword(project.getDatabasePassword());
            hds.addDataSourceProperty("serverTimezone", project.getDatabaseServerTimezone().getLiteral());

            for (Iterator<MutableTriple<DriverProperty, UUID, PojoStatus>> iterator = driverPropertiesWithStatus.iterator(); iterator.hasNext(); ) {
                MutableTriple<DriverProperty, UUID, PojoStatus> triple = iterator.next();

                DriverProperty driverProperty = triple.getLeft();
                PojoStatus pojoStatus = triple.getRight();

                if(pojoStatus == TO_REMOVE && Objects.isNull(driverProperty.getId())) {
                    iterator.remove();
                    continue;
                }

                if (StringUtils.isBlank(driverProperty.getKey())) {
                    if(Objects.isNull(driverProperty.getId()))
                        iterator.remove();
                    else
                        triple.setRight(TO_REMOVE);
                } else if(pojoStatus != TO_REMOVE) {
                    if (StringUtils.isBlank(driverProperty.getValue()))
                        driverProperty.setValue("");

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

            DSLContext statsDsl = configJooqContext(hds, SQLDialect.MYSQL, project.getDatabaseSchema(), 10);

            statsDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                tablesCount = transactionalDsl.selectCount()
                        .from(DSL.table("information_schema.TABLES"))
                        .where(DSL.field("TABLE_SCHEMA").eq(project.getDatabaseSchema()),
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
                    "Failed validation project [" + project.getId() + "]",
                    e.toString()));

            return false;
        }

        if(tablesCount == 5) {
            fc.addMessage("msgs", new FacesMessage("Project [" + project.getId() + "] validated", ""));
            connectionValidated = true;

            return true;
        }

        fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                "Failed validation project [" + project.getId() + "]",
                "One of 5 database tables is missing"));

        return false;
    }

    public void save() {
        FacesContext fc = FacesContext.getCurrentInstance();

        try {
            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                transactionalDsl.update(PROJECT)
                        .set(PROJECT.NAME, project.getName())
                        .set(PROJECT.DESCRIPTION, StringUtils.isBlank(project.getDescription()) ? null : project.getDescription())
                        .set(PROJECT.DATABASE_HOSTPORT, project.getDatabaseHostport())
                        .set(PROJECT.DATABASE_SCHEMA, project.getDatabaseSchema())
                        .set(PROJECT.DATABASE_PASSWORD, project.getDatabasePassword())
                        .set(PROJECT.DATABASE_SERVER_TIMEZONE, project.getDatabaseServerTimezone())
                        .where(PROJECT.ID.eq(project.getId()))
                        .execute();

                List<UInteger> toRemoveDriverPropertyIds = new ArrayList<>(driverPropertiesWithStatus.size());
                for (Iterator<MutableTriple<DriverProperty, UUID, PojoStatus>> iterator = driverPropertiesWithStatus.iterator(); iterator.hasNext(); ) {
                    MutableTriple<DriverProperty, UUID, PojoStatus> triple = iterator.next();

                    if(triple.getRight() == TO_REMOVE) {
                        toRemoveDriverPropertyIds.add(triple.getLeft().getId());
                        iterator.remove();
                    }
                }

                if(!toRemoveDriverPropertyIds.isEmpty()) {
                    transactionalDsl.deleteFrom(DRIVER_PROPERTY)
                            .where(DRIVER_PROPERTY.ID.in(toRemoveDriverPropertyIds))
                            .execute();
                }

                for (MutableTriple<DriverProperty, UUID, PojoStatus> triple : driverPropertiesWithStatus) {
                    DriverProperty driverProperty = triple.getLeft();
                    PojoStatus pojoStatus = triple.getRight();

                    if(pojoStatus == EXISTED) {
                        transactionalDsl.update(DRIVER_PROPERTY)
                                .set(DRIVER_PROPERTY.KEY, driverProperty.getKey())
                                .set(DRIVER_PROPERTY.VALUE, driverProperty.getValue())
                                .where(DRIVER_PROPERTY.ID.eq(driverProperty.getId()))
                                .execute();
                    } else if(pojoStatus == NEW) {
                        transactionalDsl.insertInto(DRIVER_PROPERTY)
                                .set(DRIVER_PROPERTY.KEY, driverProperty.getKey())
                                .set(DRIVER_PROPERTY.VALUE, driverProperty.getValue())
                                .set(DRIVER_PROPERTY.PROJECT_ID, project.getId())
                                .execute();
                    }
                }

                driverPropertiesWithStatus.clear();
                fetchDriverProperties();
            });

            fc.addMessage("msgs", new FacesMessage("Project [" + project.getId() + "] saved", ""));
        } catch (Exception e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save project [" + project.getId() + "]",
                    e.toString()));
        } finally {
            connectionValidated = false;
        }
    }

    public void onAddProperty() {
        connectionValidated = false;
        driverPropertiesWithStatus.add(MutableTriple.of(new DriverProperty(), UUID.randomUUID(), NEW));
    }

    public void onRemoveProperty(UUID driverPropertyGid) {
        connectionValidated = false;
        driverPropertiesWithStatus
                .stream()
                .filter(triple -> triple.getMiddle().equals(driverPropertyGid))
                .forEach(triple -> triple.setRight(TO_REMOVE));
    }
}