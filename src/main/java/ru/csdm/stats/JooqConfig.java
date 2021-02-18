package ru.csdm.stats;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import static ru.csdm.stats.common.utils.SomeUtils.buildHikariDataSource;
import static ru.csdm.stats.common.utils.SomeUtils.configJooqContext;


@Configuration
public class JooqConfig {
    @Bean
    @Lazy(false)
    @DependsOn("collectorDataSource")
    DSLContext collectorDsl(HikariDataSource collectorDataSource) {
        return configJooqContext(collectorDataSource, SQLDialect.MYSQL, null, 30);
    }

    @Bean
    @ConfigurationProperties("collector.datasource")
    @DependsOn("coreExecutor")
    public HikariDataSource collectorDataSource() {
        return buildHikariDataSource("collector-pool");
    }
}