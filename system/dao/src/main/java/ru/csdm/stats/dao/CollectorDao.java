package ru.csdm.stats.dao;

import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record2;
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
import java.util.Objects;

import static ru.csdm.stats.common.model.collector.tables.DriverProperty.DRIVER_PROPERTY;
import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@Repository
@Slf4j
public class CollectorDao {
    @Autowired
    private DSLContext collectorDsl;

    public CollectorData fetchCollectorData(UInteger instanceId, UInteger projectId) {
        if(Objects.isNull(instanceId) && Objects.isNull(projectId))
            throw new IllegalArgumentException("One of instanceId or projectId required");

        CollectorData collectorData = new CollectorData();

        collectorDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            Condition instanceIdCondition;
            if(Objects.nonNull(instanceId))
                instanceIdCondition = KNOWN_SERVER.INSTANCE_ID.eq(instanceId);
            else
                instanceIdCondition = DSL.trueCondition();

            Condition projectIdCondition;
            if(Objects.nonNull(projectId))
                projectIdCondition = KNOWN_SERVER.PROJECT_ID.eq(projectId);
            else
                projectIdCondition = DSL.trueCondition();

            List<KnownServer> knownServers = transactionalDsl
                    .select(KNOWN_SERVER.asterisk())
                    .from(KNOWN_SERVER)
                    .where(instanceIdCondition, projectIdCondition)
                    .orderBy(KNOWN_SERVER.PROJECT_ID.desc(), KNOWN_SERVER.ID.asc())
                    .fetchInto(KnownServer.class);

            Map<UInteger, Project> projectByProjectId = transactionalDsl
                    .select(PROJECT.asterisk())
                    .from(PROJECT)
                    .join(KNOWN_SERVER).on(PROJECT.ID.eq(KNOWN_SERVER.PROJECT_ID))
                    .where(instanceIdCondition, projectIdCondition)
                    .groupBy(PROJECT.ID)
                    .fetchMap(PROJECT.ID, Project.class);

            Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId = transactionalDsl
                    .select(DRIVER_PROPERTY.asterisk())
                    .from(DRIVER_PROPERTY)
                    .join(KNOWN_SERVER).on(DRIVER_PROPERTY.PROJECT_ID.eq(KNOWN_SERVER.PROJECT_ID))
                    .where(instanceIdCondition, projectIdCondition)
                    .groupBy(DRIVER_PROPERTY.ID)
                    .fetchGroups(DRIVER_PROPERTY.PROJECT_ID, DriverProperty.class);

            collectorData.setKnownServers(knownServers);
            collectorData.setProjectByProjectId(projectByProjectId);
            collectorData.setDriverPropertiesByProjectId(driverPropertiesByProjectId);
        });

        return collectorData;
    }

    public Record2<Integer, Integer> fetchKnownServersCounts(UInteger projectId,
                                                             UInteger currentInstanceId) {
        return collectorDsl.select(
                DSL.selectCount()
                        .from(KNOWN_SERVER)
                        .join(INSTANCE).on(KNOWN_SERVER.INSTANCE_ID.eq(INSTANCE.ID))
                        .where(KNOWN_SERVER.PROJECT_ID.eq(projectId),
                                INSTANCE.ID.eq(currentInstanceId))
                        .<Integer>asField("at_instance"),
                DSL.selectCount()
                        .from(KNOWN_SERVER)
                        .where(KNOWN_SERVER.PROJECT_ID.eq(projectId))
                        .<Integer>asField("at_all_instances")
        ).fetchOne();
    }

    public Map<UInteger, String> fetchKnownServersNames(UInteger instanceId) {
        return collectorDsl.select(KNOWN_SERVER.ID, KNOWN_SERVER.NAME)
                .from(KNOWN_SERVER)
                .where(KNOWN_SERVER.INSTANCE_ID.eq(instanceId))
                .fetchMap(KNOWN_SERVER.ID, KNOWN_SERVER.NAME);
    }
}