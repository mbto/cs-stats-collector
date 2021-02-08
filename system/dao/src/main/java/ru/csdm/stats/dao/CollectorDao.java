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
import ru.csdm.stats.service.InstanceHolder;

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
    @Autowired
    private InstanceHolder instanceHolder;

    public CollectorData fetchCollectorData() {
        CollectorData collectorData = new CollectorData();

        collectorDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger currentInstanceId = instanceHolder.getCurrentInstanceId();

            List<KnownServer> knownServers = transactionalDsl.select(KNOWN_SERVER.asterisk())
                    .from(KNOWN_SERVER)
                    .join(INSTANCE).on(INSTANCE.ID.eq(KNOWN_SERVER.INSTANCE_ID))
                    .where(KNOWN_SERVER.ACTIVE.eq(true),
                            INSTANCE.ID.eq(currentInstanceId))
                    .fetchInto(KnownServer.class);

            Map<UInteger, Project> projectByProjectId = transactionalDsl.select(PROJECT.asterisk())
                    .from(PROJECT)
                    .join(KNOWN_SERVER).on(KNOWN_SERVER.PROJECT_ID.eq(PROJECT.ID))
                    .join(INSTANCE).on(INSTANCE.ID.eq(KNOWN_SERVER.INSTANCE_ID))
                    .where(INSTANCE.ID.eq(currentInstanceId))
                    .groupBy(PROJECT.ID)
                    .fetchMap(PROJECT.ID, Project.class);

            Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId = collectorDsl.select(DRIVER_PROPERTY.asterisk())
                    .from(DRIVER_PROPERTY)
                    .join(KNOWN_SERVER).on(KNOWN_SERVER.PROJECT_ID.eq(DRIVER_PROPERTY.PROJECT_ID))
                    .join(INSTANCE).on(INSTANCE.ID.eq(KNOWN_SERVER.INSTANCE_ID))
                    .where(INSTANCE.ID.eq(currentInstanceId))
                    .groupBy(DRIVER_PROPERTY.ID)
                    .fetchGroups(DRIVER_PROPERTY.PROJECT_ID, DriverProperty.class);

            collectorData.setKnownServers(knownServers);
            collectorData.setProjectByProjectId(projectByProjectId);
            collectorData.setDriverPropertiesByProjectId(driverPropertiesByProjectId);
        });

        return collectorData;
    }
}