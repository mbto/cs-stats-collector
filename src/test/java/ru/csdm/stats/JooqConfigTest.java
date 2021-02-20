package ru.csdm.stats;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import static ru.csdm.stats.common.utils.SomeUtils.buildHikariDataSource;
import static ru.csdm.stats.common.utils.SomeUtils.configJooqContext;

@Profile("test")
@Configuration
public class JooqConfigTest {
    @Bean
    @Lazy(false)
    @DependsOn("collectorAdminDataSource")
    DSLContext collectorAdminDsl(HikariDataSource collectorAdminDataSource) {
        return configJooqContext(collectorAdminDataSource, SQLDialect.MYSQL, null, 10);
    }

    @Bean
    @ConfigurationProperties("collector.admin.datasource")
    @DependsOn("brokerTE")
    public HikariDataSource collectorAdminDataSource() {
        return buildHikariDataSource("collector-admin-pool");
    }
}