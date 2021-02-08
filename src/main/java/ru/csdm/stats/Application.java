package ru.csdm.stats;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.DatagramsQueue;
import ru.csdm.stats.common.dto.ServerData;

import javax.faces.webapp.FacesServlet;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ThreadPoolExecutor;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

@SpringBootApplication(exclude = {JooqAutoConfiguration.class, TaskExecutionAutoConfiguration.class})
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
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
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Pool used, when merging players into the csstats.* tables.
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

    @Bean(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        /* Reducing the number of threads in the standard spring-boot applicationTaskExecutor pool
           from auto-configuration org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
           org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Pool#maxSize = Integer.MAX_VALUE */
        int poolSize = 2; /* 1-spring-boot framework; 2-for main listener; */
        // todo: +1 scheduler
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadNamePrefix("appExecutor-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        };
    }

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