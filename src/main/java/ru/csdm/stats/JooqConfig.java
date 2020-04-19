package ru.csdm.stats;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.boot.autoconfigure.jooq.JooqExceptionTranslator;
import org.springframework.boot.autoconfigure.jooq.SpringTransactionProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

@Configuration
public class JooqConfig {

    @Bean
    @Lazy(false)
    @DependsOn("statsDataSource")
    DSLContext statsDsl(HikariDataSource statsDataSource) {
        return configJooqContext(statsDataSource, SQLDialect.MYSQL);
    }

    @Bean
    @ConfigurationProperties("stats.datasource")
    @DependsOn(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public HikariDataSource statsDataSource() {
        HikariDataSource ds = DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .type(HikariDataSource.class)
                .build();
/* 1 connection to database in pool, due exclude conflicts / deadlocks
on identical players names on insert statements in parallel threads */
        ds.setMaximumPoolSize(1);
        ds.setPoolName("stats-pool");
        return ds;
    }

    private static DefaultDSLContext configJooqContext(DataSource dataSource, SQLDialect dialect) {
        DefaultConfiguration config = new DefaultConfiguration();
        config.set(new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource)));
        config.set(new DefaultExecuteListenerProvider(new JooqExceptionTranslator()));
        config.set(new SpringTransactionProvider(new DataSourceTransactionManager(dataSource)));
        config.setSQLDialect(dialect);
        return new DefaultDSLContext(config);
    }
}