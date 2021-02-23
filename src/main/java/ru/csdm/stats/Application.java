package ru.csdm.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.MessageQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.ServerData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

@SpringBootApplication(exclude = {JooqAutoConfiguration.class,
        TaskExecutionAutoConfiguration.class, TaskSchedulingAutoConfiguration.class})
@EnableAsync(proxyTargetClass = true)
//@EnableScheduling
public class Application {
    static {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public BlockingDeque<Message<?>> brokerQueue() {
        return new LinkedBlockingDeque<>(Integer.MAX_VALUE);
    }
    /**
     * Relationship registry
     * Key: Server address (ip:port)
     * Value: null or ServerData
     */
    @Bean
    public Map<String, ServerData> serverDataByAddress() {
        return new ConcurrentSkipListMap<>();
    }
    /**
     * Relationship registry
     * Key: Server address (ip:port)<br/>
     * Value: null or MessageQueue
     */
    @Bean
    public Map<String, MessageQueue> messageQueueByAddress() {
        return new ConcurrentSkipListMap<>(); // TODO: check for LinkedHashMap
    }
    /**
     * MessageQueue registry
     * Key: Queue ID<br/>
     * Value: MessageQueue
     */
    @Bean
    public Map<Integer, MessageQueue> messageQueueByQueueId() {
        return new LinkedHashMap<>();
    }
    /**
     * Relationship registry
     * Key: Server address (ip:port)
     * Value: null or Map&lt;Player nick, CollectedPlayer&gt;
     */
    @Bean
    public Map<String, Map<String, CollectedPlayer>> gameSessionByAddress() {
        return new ConcurrentSkipListMap<>();
    }
    /**
     * Pool used in 2 cases:
     * 1 - consume from UDP port -> send to brokerQueue;
     * 2 - consume from brokerQueue -> distribute to messageQueues;
     * TODO: 3 - scheduler
     */
    @Bean("brokerTE")
    public ThreadPoolTaskExecutor brokerTE() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int poolSize = 2; // todo: +1 scheduler
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadNamePrefix("brokerTE-");
        executor.setDaemon(false);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
    /**
     * Pool used in Broker and DatagramsConsumer classes
     * 1 - consume from messageQueues
     *     -> accumulate players statistics and sessions
     *     -> sending to senderTE pool;
     */
    @Bean
    @DependsOn("senderTE")
    public ThreadPoolTaskExecutor consumerTE() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        /* Pool sizes changes automatically, depends on the number of active HLDS servers (in table collector.known_server)
            AND the number of processors */
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(1);

        executor.setThreadGroupName("consumers");
        executor.setThreadNamePrefix("consumer-");
        executor.setDaemon(false);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
    /**
     * Pool used in PlayersSender class
     * 1 - consume players sessions from DatagramsConsumer
     *     -> merging players sessions into the csstats.* tables.
     */
    @Bean
    @DependsOn("collectorDsl")
    public ThreadPoolTaskExecutor senderTE(
            @Value("${collector.datasource.maximumPoolSize}") int datasourceMaximumPoolSize
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int poolSize = Math.max(1,
                Math.min(datasourceMaximumPoolSize, Runtime.getRuntime().availableProcessors())
        );
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadGroupName("playersSenders");
        executor.setThreadNamePrefix("playerSender-");
        executor.setDaemon(false);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
//        return builder -> {
//            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        };
//    }

    @Bean
    public ServletContextInitializer withParamsContextInitializer() {
        return servletContext -> {
//            servletContext.setInitParameter("javax.faces.DEFAULT_SUFFIX", ".xhtml");
            servletContext.setInitParameter("javax.faces.FACELETS_VIEW_MAPPINGS", "*.xhtml");
            servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Production");
//        servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
            servletContext.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", "true");
            servletContext.setInitParameter("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
            servletContext.setInitParameter("javax.faces.FACELETS_BUFFER_SIZE", "65535");

            // Bug in ELResolver https://stackoverflow.com/questions/19575283/jsf-2-2-interpret-empty-string-submitted-values-as-null-not-working
            // solutions don't work =/
            servletContext.setInitParameter("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");

            servletContext.setInitParameter("com.sun.faces.enableRestoreView11Compatibility", "true");
            servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", "true");

            servletContext.setInitParameter("primefaces.THEME", "glass-x");
            servletContext.setInitParameter("primefaces.SUBMIT", "partial");
            servletContext.setInitParameter("primefaces.TRANSFORM_METADATA", "true");
//            servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");
            servletContext.setInitParameter("primefaces.UPLOADER", "native");

//            servletContext.setInitParameter("org.omnifaces.FACES_VIEWS_SCAN_PATHS", "/*.xhtml/*");
        };
    }
}