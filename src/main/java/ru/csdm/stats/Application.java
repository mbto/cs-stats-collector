package ru.csdm.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.DatagramsQueue;
import ru.csdm.stats.common.dto.ServerData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ThreadPoolExecutor;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

@SpringBootApplication(exclude = {JooqAutoConfiguration.class, TaskExecutionAutoConfiguration.class})
@EnableAsync(proxyTargetClass = true)
@EnableCaching
public class Application {
    static {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Key: Server address (ip:port)
     * Value: ServerData
     */
    @Bean
    public Map<String, ServerData> availableAddresses() {
        return new ConcurrentSkipListMap<>();
    }
    /**
     * Key: Server address (ip:port)
     * Value: Queue ID
     */
    @Bean
    public Map<String, Integer> registeredAddresses() {
        return new LinkedHashMap<>();
    }
    /**
     * Key: Queue ID
     * Value: DatagramsQueue
     */
    @Bean
    public Map<Integer, DatagramsQueue> datagramsInQueuesById() {
        return new LinkedHashMap<>();
    }

    /**
     * Key: Server address (ip:port)
     * Value: Map<Player nick, Player>
     */
    @Bean
    public Map<String, Map<String, CollectedPlayer>> gameSessionByAddress() {
        return new ConcurrentSkipListMap<>();
    }

    /**
     * Pool used, when HLDS servers sends logs by UDP to cs-stats-collector listener.
     */
    @Bean
    @DependsOn("playersSenderTaskExecutor")
    public ThreadPoolTaskExecutor consumerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        /* Pool sizes changes automatically, depends on the number of active HLDS servers (in table csstats.known_server)
            AND the number of processors */
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);

        executor.setThreadGroupName("consumers");
        executor.setThreadNamePrefix("consumer-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Pool used, when merging players into the csstats.* tables.
     */
    @Bean
    @DependsOn("statsDataSource")
    public ThreadPoolTaskExecutor playersSenderTaskExecutor(
            @Value("${stats.datasource.maximumPoolSize}") int datasourceMaximumPoolSize
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int poolSize = Math.max(1,
                Math.min(datasourceMaximumPoolSize, Runtime.getRuntime().availableProcessors())
        );
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadGroupName("playersSenders");
        executor.setThreadNamePrefix("playerSender-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        /* Reducing the number of threads in the standard spring-boot applicationTaskExecutor pool
           from auto-configuration org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
           org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Pool#maxSize = Integer.MAX_VALUE */
        int poolSize = 2; /* 1-spring-boot framework; 2-for main listener; */
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadNamePrefix("appExecutor-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
}