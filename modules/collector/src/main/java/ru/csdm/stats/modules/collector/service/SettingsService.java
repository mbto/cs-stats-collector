package ru.csdm.stats.modules.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.dto.ServerSetting;
import ru.csdm.stats.dao.CsStatsDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Lazy(false)
@Slf4j
public class SettingsService {
    @Autowired
    private CsStatsDao csStatsDao;
    @Autowired
    private Map<String, ServerData> availableAddresses;

    public void updateSettings(boolean firstLoading) {
        log.info("Updating servers settings from database");

        List<ServerSetting> serversSettings = null;
        try {
            serversSettings = csStatsDao.fetchServersSettings();
        } catch (DataAccessException e) {
            log.warn("Unable to fetch servers settings from database", e);
        }

        if(Objects.nonNull(serversSettings)) {
            LocalDateTime now = LocalDateTime.now();
            Map<String, ServerData> serversDatas = serversSettings.stream()
                    .collect(Collectors.toMap(ServerSetting::getIpport, serverSetting -> {
                        ServerData serverData = new ServerData();
                        serverData.setServerSetting(serverSetting);
                        serverData.setLastTouchDateTime(now);
                        serverData.setListening(true);

                        return serverData;
                    }));

            if(!firstLoading) { // deactive existed serversDatas on second and further loading
                for (Map.Entry<String, ServerData> entry : availableAddresses.entrySet()) {
                    ServerData serverData = entry.getValue();

                    if(!serverData.isListening())
                        continue;

                    String address = entry.getKey();

                    if(!serversDatas.containsKey(address)) {
                        serverData.setListening(false);

                        log.info(serverData.getServerSetting().getIpport() + " listening stopped");
                    }
                }
            }

            if(!serversDatas.isEmpty()) {
                for (Map.Entry<String, ServerData> entry : serversDatas.entrySet()) { // create/update all serversDatas
                    String newAddress = entry.getKey();
                    ServerData existedServerData = availableAddresses.get(newAddress);
                    ServerData newServerData = entry.getValue();

                    if(Objects.isNull(existedServerData)) {
                        availableAddresses.put(newAddress, newServerData);

                        if(!firstLoading)
                            log.info(newServerData.getServerSetting().getIpport() + " added to listening");
                    } else {
                        ServerSetting existedServerSetting = existedServerData.getServerSetting();
                        ServerSetting newServerSetting = newServerData.getServerSetting();
                        existedServerSetting.applyNewValues(newServerSetting);

                        if(!existedServerData.isListening()) {
                            existedServerData.setListening(true);

                            log.info(existedServerData.getServerSetting().getIpport() + " listening started");
                        }
                    }
                }
            }
        }

        if(availableAddresses.isEmpty()) {
            log.info("No available servers with settings");
        } else {
            log.info("Known " + availableAddresses.size() +
                    " server" + (availableAddresses.size() > 1 ? "s" : "") +" with settings:");

            for (ServerData serverData : availableAddresses.values()) {
                log.info(String.format("%-15s", serverData.isListening() ? "[LISTENING]" : "[NOT LISTENING]")
                        + " " + serverData.getServerSetting().toString());
            }
        }
    }
}