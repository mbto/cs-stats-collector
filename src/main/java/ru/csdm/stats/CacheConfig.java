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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        List<CaffeineCache> caches = new ArrayList<>();
        caches.add(new CaffeineCache("instances", Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build()));

        if(Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            log.info("Caffeine managers cache disabled");
        } else {
            log.info("Activating caffeine managers cache");

            int managersCount = collectorDsl.fetchCount(MANAGER);

            caches.add(new CaffeineCache("managers", Caffeine.newBuilder()
                    .maximumSize(Math.max(10, managersCount + 5))
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .build()));
        }

        cacheManager.setCaches(caches);

        return cacheManager;
    }
}