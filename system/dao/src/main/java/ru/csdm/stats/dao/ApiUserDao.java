package ru.csdm.stats.dao;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.model.tables.pojos.ApiUser;

import static ru.csdm.stats.common.model.tables.ApiUser.API_USER;

@Profile("default")
@Repository
@Slf4j
public class ApiUserDao {
    @Autowired
    private DSLContext statsDsl;

    @Cacheable(value = "apiUsers", key = "#username")
    public ApiUser findApiUserByUsername(String username) {
        return statsDsl.selectFrom(API_USER)
                .where(API_USER.USERNAME.eq(username))
                .fetchOneInto(ApiUser.class);
    }
}