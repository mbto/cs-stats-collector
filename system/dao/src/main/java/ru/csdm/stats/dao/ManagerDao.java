package ru.csdm.stats.dao;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.model.collector.tables.pojos.Manager;

import static ru.csdm.stats.common.model.collector.tables.Manager.MANAGER;

@Profile("default")
@Repository
@Slf4j
public class ManagerDao {
    @Autowired
    private DSLContext collectorDsl;

    public Manager findManagerUsername(String username) {
        return collectorDsl.selectFrom(MANAGER)
                .where(MANAGER.USERNAME.eq(username))
                .fetchOneInto(Manager.class);
    }
}