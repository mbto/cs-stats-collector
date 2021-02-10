package ru.csdm.stats.modules.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.modules.collector.handlers.Listener;
import ru.csdm.stats.service.SettingsService;

import javax.annotation.PostConstruct;

@Service
@Lazy(false)
@Slf4j
public class Module {
    @Autowired
    private Listener listener;
    @Autowired
    private SettingsService settingsService;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("init() start");

        log.info("Available processors: " + Runtime.getRuntime().availableProcessors());

        settingsService.updateSettings(true);

        listener.launchAsync();

        if(log.isDebugEnabled())
            log.debug("init() finish");
    }
}