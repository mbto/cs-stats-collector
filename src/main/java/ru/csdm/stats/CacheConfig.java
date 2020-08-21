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

import static ru.csdm.stats.common.model.collector.tables.Manager.MANAGER;

@Configuration
@Slf4j
public class CacheConfig {
    @Autowired
    private Environment environment;

    @Autowired
    private DSLContext collectorDsl;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        if(Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            log.info("Caffeine cache disabled");
        } else {
            log.info("Activating caffeine cache");

            int managersCount = collectorDsl.fetchCount(MANAGER);

            cacheManager.setCaches(Arrays.asList(
                    new CaffeineCache("managers", Caffeine.newBuilder()
                            .maximumSize(Math.max(10, managersCount + 5))
                            .expireAfterWrite(30, TimeUnit.MINUTES)
                            .build())
            ));
        }

        return cacheManager;
    }
}