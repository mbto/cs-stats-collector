package ru.csdm.stats;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static ru.csdm.stats.common.model.tables.ApiUser.API_USER;

@Configuration
@Slf4j
public class CacheConfig {
    @Autowired
    private Environment environment;

    @Autowired
    private DSLContext statsDsl;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        if(Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            log.info("Caffeine cache disabled");
        } else {
            log.info("Activating caffeine cache");

            int apiUsersCount = statsDsl.fetchCount(API_USER);

            cacheManager.setCaches(Arrays.asList(
                    new CaffeineCache("apiUsers", Caffeine.newBuilder()
                            .maximumSize(Math.max(10, apiUsersCount + 5))
                            .expireAfterWrite(5, TimeUnit.MINUTES)
                            .build())
            ));
        }

        return cacheManager;
    }
}