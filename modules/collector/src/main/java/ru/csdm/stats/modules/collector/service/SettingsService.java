package ru.csdm.stats.modules.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.ServerSetting;
import ru.csdm.stats.dao.AmxDao;

import java.util.Map;

@Service
@Lazy(false)
@Slf4j
public class SettingsService {
    @Autowired
    private AmxDao amxDao;
    @Autowired
    private Map<String, ServerSetting> availableAddresses;

    public void updateSettings(boolean firstLoading) {
        log.info("Updating servers settings from database");

        try {
            Map<String, ServerSetting> serverInfos = amxDao.fetchServersSettings();

            if(!serverInfos.isEmpty()) {
                if(!firstLoading) { // remove old elements when refreshing
                    availableAddresses
                            .entrySet()
                            .removeIf(address -> !serverInfos.containsKey(address.getKey()));
                }

                availableAddresses.putAll(serverInfos); // replace all settings
            }
        } catch (DataAccessException e) {
            log.warn("Unable to fetch servers settings from database", e);
        }

        if(availableAddresses.isEmpty()) {
            log.info("No available servers with settings");
        } else {
            log.info("Used " + availableAddresses.size() +
                    " server" + (availableAddresses.size() > 1 ? "s" : "" ) +" with settings:");

            for (ServerSetting serverSetting : availableAddresses.values()) {
                log.info(serverSetting.toString());
            }
        }
    }
}