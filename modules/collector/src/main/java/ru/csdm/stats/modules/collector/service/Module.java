package ru.csdm.stats.modules.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.ServerSetting;
import ru.csdm.stats.dao.AmxDao;
import ru.csdm.stats.modules.collector.handlers.Listener;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@Lazy(false)
@Slf4j
public class Module {
    @Autowired
    private Listener listener;
    @Autowired
    private AmxDao amxDao;

    @Autowired
    private Map<String, ServerSetting> availableAddresses;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("init() start");

        try {
            Map<String, ServerSetting> serverInfos = amxDao.fetchAvailableAddresses();

            if(!serverInfos.isEmpty()) {
                availableAddresses.putAll(serverInfos);
            }
        } catch (DataAccessException e) {
            log.warn("Unable to fetch ipport's from database", e);
        }

        if(availableAddresses.isEmpty()) {
            log.info("No available addresses");
        } else {
            log.info("Available " + availableAddresses.size() +
                    " address" + (availableAddresses.size() > 1 ? "es" : "" ) +":");

            for (ServerSetting serverSetting : availableAddresses.values()) {
                log.info(serverSetting.toString());
            }
        }

        listener.launchAsync();

        if(log.isDebugEnabled())
            log.debug("init() finish");
    }
}