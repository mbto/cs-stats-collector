package ru.csdm.stats.dao;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;

import java.util.Map;

import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;

@Repository
@Slf4j
public class InstanceDao {
    @Autowired
    private DSLContext collectorDsl;

    @Cacheable(value = "instances")
    public Map<UInteger, Instance> findAll() {
        if(log.isDebugEnabled())
            log.debug("\nfindInstances");

        return collectorDsl.selectFrom(INSTANCE)
                .orderBy(INSTANCE.ID.asc())
                .fetchMap(INSTANCE.ID, Instance.class);
    }
}