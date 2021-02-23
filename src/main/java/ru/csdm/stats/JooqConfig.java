package ru.csdm.stats;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import static ru.csdm.stats.common.utils.SomeUtils.buildHikariDataSource;
import static ru.csdm.stats.common.utils.SomeUtils.configJooqContext;

@Configuration
public class JooqConfig {
    @Bean
    @Lazy(false)
    DSLContext collectorDsl(HikariDataSource collectorDataSource) {
        return configJooqContext(collectorDataSource, SQLDialect.MYSQL, null, 30);
    }

    @ConfigurationProperties("collector.datasource")
    @Bean
    @DependsOn("brokerTE")
    public HikariDataSource collectorDataSource() {
        return buildHikariDataSource("collector-pool");
    }
}