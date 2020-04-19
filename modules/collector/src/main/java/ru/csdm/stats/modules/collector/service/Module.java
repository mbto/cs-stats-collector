package ru.csdm.stats.modules.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.dao.AmxDao;
import ru.csdm.stats.modules.collector.handlers.Listener;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Service
@Lazy(false)
@Slf4j
public class Module {
    @Autowired
    private Listener listener;
    @Autowired
    private AmxDao amxDao;

    @Autowired
    private Set<String> availableAddresses;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("init() start");

        List<String> ipports = null;
        try {
            ipports = amxDao.fetchAvailableAddresses();

            if(!ipports.isEmpty()) {
                availableAddresses.addAll(ipports);
            }
        } catch (DataAccessException e) {
            log.warn("Unable to fetch ipport's from database", e);
        }

        if(availableAddresses.isEmpty()) {
            log.info("No available addresses");
        } else {
            log.info("Available " + availableAddresses.size() +
                    " address" + (availableAddresses.size() > 1 ? "es" : "" ) +":");

            for (String availableIp : availableAddresses) {
                log.info(availableIp);
            }
        }

        listener.launchAsync();

        if(log.isDebugEnabled())
            log.debug("init() finish");
    }
}