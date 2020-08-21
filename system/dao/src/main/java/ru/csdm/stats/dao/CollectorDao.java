package ru.csdm.stats.dao;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.dto.CollectorData;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;

import java.util.List;
import java.util.Map;

import static ru.csdm.stats.common.model.collector.tables.DriverProperty.DRIVER_PROPERTY;
import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@Repository
@Slf4j
public class CollectorDao {
    @Autowired
    private DSLContext collectorDsl;

    public CollectorData fetchCollectorData(String collectorInstanceName) {
        CollectorData collectorData = new CollectorData();

        collectorDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            List<KnownServer> knownServers = transactionalDsl.select(KNOWN_SERVER.asterisk())
                    .from(KNOWN_SERVER)
                    .join(INSTANCE).on(INSTANCE.ID.eq(KNOWN_SERVER.INSTANCE_ID))
                    /* joining is optional, since `known_server`.`project_id` int unsigned NOT NULL */
                    //.join(PROJECT).on(KNOWN_SERVER.PROJECT_ID.eq(PROJECT.ID))
                    .where(KNOWN_SERVER.ACTIVE.eq(true),
                            INSTANCE.NAME.eq(collectorInstanceName))
                    .fetchInto(KnownServer.class);

            Map<UInteger, Project> projectByProjectId = transactionalDsl.select(PROJECT.asterisk())
                    .from(PROJECT)
                    .join(KNOWN_SERVER).on(KNOWN_SERVER.PROJECT_ID.eq(PROJECT.ID))
                    .join(INSTANCE).on(INSTANCE.ID.eq(KNOWN_SERVER.INSTANCE_ID))
                    .where(INSTANCE.NAME.eq(collectorInstanceName))
                    .groupBy(PROJECT.ID)
                    .fetchMap(PROJECT.ID, Project.class);

            Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId = collectorDsl.select(DRIVER_PROPERTY.asterisk())
                    .from(DRIVER_PROPERTY)
                    .join(KNOWN_SERVER).on(KNOWN_SERVER.PROJECT_ID.eq(DRIVER_PROPERTY.PROJECT_ID))
                    .join(INSTANCE).on(INSTANCE.ID.eq(KNOWN_SERVER.INSTANCE_ID))
                    .where(INSTANCE.NAME.eq(collectorInstanceName))
                    .groupBy(DRIVER_PROPERTY.ID)
                    .fetchGroups(DRIVER_PROPERTY.PROJECT_ID, DriverProperty.class);

            collectorData.setKnownServers(knownServers);
            collectorData.setProjectByProjectId(projectByProjectId);
            collectorData.setDriverPropertiesByProjectId(driverPropertiesByProjectId);
        });

        return collectorData;
    }
}