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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.MessageQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.ServerData;

import java.net.DatagramPacket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
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
    public BlockingQueue<Message<?>> listenerQueue() {
        return new LinkedBlockingQueue<>(Integer.MAX_VALUE);
    }
    /**
     * Key: Server address (ip:port)
     * Value: ServerData
     */
    @Bean
    public Map<String, ServerData> serverDataByAddress() {
        return new ConcurrentSkipListMap<>();
    }
    /**
     * Key: Server address (ip:port)<br/>
     * Value: MessageQueue&lt;Message&lt;?&gt;&gt;
     */
    @Bean
    public Map<String, MessageQueue<Message<?>>> messageQueueByAddress() {
        return new ConcurrentSkipListMap<>(); // TODO: check for LinkedHashMap
    }
    /**
     * Key: Queue ID<br/>
     * Value: MessageQueue&lt;Message&lt;?&gt;&gt;
     */
    @Bean
    public Map<Integer, MessageQueue<Message<?>>> messageQueueByQueueId() {
        return new LinkedHashMap<>();
    }
    /**
     * Key: Server address (ip:port)
     * Value: Map&lt;Player nick, Player&gt;
     */
    @Bean
    public Map<String, Map<String, CollectedPlayer>> gameSessionByAddress() {
        return new ConcurrentSkipListMap<>();
    }
    /**
     * Pool used in Listener and DatagramsConsumer classes
     * 1 - consume from messageQueues
     *     -> accumulate players statistics and sessions
     *     -> sending to playersSenderTaskExecutor pool;
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
    @DependsOn("collectorDataSource")
    public ThreadPoolTaskExecutor playersSenderTaskExecutor(
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
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
    /**
     * Pool used in 2 cases:
     * 1 - consume from UDP port -> send to listenerQueue;
     * 2 - consume from listenerQueue -> distribute to messageQueues;
     * TODO: 3 - scheduler
     */
    @Bean("coreExecutor")
    public ThreadPoolTaskExecutor coreExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int poolSize = 2; // todo: +1 scheduler
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadNamePrefix("coreExecutor-");
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