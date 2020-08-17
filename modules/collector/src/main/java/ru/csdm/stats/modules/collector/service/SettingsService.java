package ru.csdm.stats.modules.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.model.tables.pojos.KnownServer;
import ru.csdm.stats.common.utils.SomeUtils;
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
    private ThreadPoolTaskExecutor consumerTaskExecutor;
    @Autowired
    private Map<String, ServerData> availableAddresses;

    @Autowired
    private CsStatsDao csStatsDao;

    public void updateSettings(boolean firstLoading) {
        log.info("Updating servers settings from database");

        List<KnownServer> knownServers = null;
        try {
            knownServers = csStatsDao.fetchKnownServers();
        } catch (DataAccessException e) {
            log.warn("Unable to fetch known servers from database", e);
        }

        if(Objects.nonNull(knownServers)) {
            LocalDateTime now = LocalDateTime.now();
            Map<String, ServerData> serversDatas = knownServers.stream()
                    .collect(Collectors.toMap(KnownServer::getIpport, knownServer -> {
                        ServerData serverData = new ServerData();
                        serverData.setKnownServer(knownServer);
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

                        log.info(serverData.getKnownServer().getIpport() + " listening stopped");
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
                            log.info(newServerData.getKnownServer().getIpport() + " added to listening");
                    } else {
                        KnownServer existedKnownServer = existedServerData.getKnownServer();
                        KnownServer newKnownServer = newServerData.getKnownServer();

                        existedKnownServer.setId(newKnownServer.getId());
                        existedKnownServer.setIpport(newKnownServer.getIpport());
                        existedKnownServer.setName(newKnownServer.getName());
                        existedKnownServer.setActive(newKnownServer.getActive());
                        existedKnownServer.setFfa(newKnownServer.getFfa());
                        existedKnownServer.setIgnoreBots(newKnownServer.getIgnoreBots());
                        existedKnownServer.setStartSessionOnAction(newKnownServer.getStartSessionOnAction());

                        if(!existedServerData.isListening()) {
                            existedServerData.setListening(true);

                            log.info(existedServerData.getKnownServer().getIpport() + " listening started");
                        }
                    }
                }
            }

            int countListening = (int) availableAddresses.values()
                    .stream()
                    .filter(ServerData::isListening)
                    .count();

            int newPoolSize = Math.max(1,
                    Math.min(countListening, Runtime.getRuntime().availableProcessors())
            );

            int oldPoolSize = consumerTaskExecutor.getCorePoolSize();
            if(newPoolSize > oldPoolSize) {
                consumerTaskExecutor.setMaxPoolSize(newPoolSize);
                consumerTaskExecutor.setCorePoolSize(newPoolSize);
            } else if(newPoolSize < oldPoolSize) {
                consumerTaskExecutor.setCorePoolSize(newPoolSize);
                consumerTaskExecutor.setMaxPoolSize(newPoolSize);
            }
        }

        if(availableAddresses.isEmpty()) {
            log.info("No available servers with settings");
        } else {
            log.info("Known " + availableAddresses.size() +
                    " server" + (availableAddresses.size() > 1 ? "s" : "") +" with settings:");

            for (ServerData serverData : availableAddresses.values()) {
                log.info(String.format("%-15s", serverData.isListening() ? "[LISTENING]" : "[NOT LISTENING]")
                        + " " + SomeUtils.knownServerToString(serverData.getKnownServer()));
            }
        }
    }
}