package ru.csdm.stats.modules.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.modules.collector.handlers.Listener;

import javax.annotation.PostConstruct;

@Service
@Lazy(false)
@Slf4j
public class Module {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Listener listener;
    @Autowired
    private SettingsService settingsService;

    @Value("${collector.instance.name}")
    private String collectorInstanceName;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("init() start");

        if(StringUtils.isBlank(collectorInstanceName)) {
            log.warn("Failed initialize module, due empty property 'collector.instance.name'");

            int code = SpringApplication.exit(applicationContext, () -> 1);
            System.exit(code);
        }

        log.info("Available processors: " + Runtime.getRuntime().availableProcessors());

        settingsService.updateSettings(true);

        listener.launchAsync();

        if(log.isDebugEnabled())
            log.debug("init() finish");
    }
}