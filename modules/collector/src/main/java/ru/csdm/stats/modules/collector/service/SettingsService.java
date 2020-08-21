package ru.csdm.stats.modules.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.CollectorData;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.common.utils.SomeUtils;
import ru.csdm.stats.dao.CollectorDao;

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
    private CollectorDao collectorDao;

    @Value("${collector.instance.name}")
    private String collectorInstanceName;

    public void updateSettings(boolean firstLoading) {
        log.info("Updating servers settings from database, instance name: " + collectorInstanceName);

        CollectorData collectorData = null;
        try {
            collectorData = collectorDao.fetchCollectorData(collectorInstanceName);
        } catch (DataAccessException e) {
            log.warn("Unable to fetch collector data from database", e);
        }

        if(Objects.nonNull(collectorData)) {
            Map<UInteger, Project> projectByProjectId = collectorData.getProjectByProjectId();
            Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId = collectorData.getDriverPropertiesByProjectId();

            LocalDateTime now = LocalDateTime.now();
            Map<String, ServerData> serverDataByIpport = collectorData.getKnownServers()
                    .stream()
                    .collect(Collectors.toMap(KnownServer::getIpport, knownServer -> {
                        ServerData serverData = new ServerData();
                        serverData.setKnownServer(knownServer);
                        serverData.setLastTouchDateTime(now);

                        serverData.setProject(projectByProjectId.get(knownServer.getProjectId()));
                        serverData.setDriverProperties(driverPropertiesByProjectId.get(knownServer.getProjectId()));

                        serverData.setListening(true);

                        return serverData;
                    }));

            if(!firstLoading) { // deactivate existed serverDataByIpport on 2-nd and further loading
                for (Map.Entry<String, ServerData> entry : availableAddresses.entrySet()) {
                    ServerData serverData = entry.getValue();

                    if(!serverData.isListening())
                        continue;

                    String address = entry.getKey();

                    if(!serverDataByIpport.containsKey(address)) { // address removed from current table state
                        serverData.setListening(false);

                        log.info(serverData.getKnownServer().getIpport() + " listening stopped");
                    }
                }
            }

            if(!serverDataByIpport.isEmpty()) {
                for (Map.Entry<String, ServerData> serverDataEntry : serverDataByIpport.entrySet()) { // create/update all serverDataByIpport
                    String newAddress = serverDataEntry.getKey();
                    ServerData existedServerData = availableAddresses.get(newAddress);
                    ServerData newServerData = serverDataEntry.getValue();

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

                        existedServerData.setProject(newServerData.getProject());
                        existedServerData.setDriverProperties(newServerData.getDriverProperties());

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
                        + " " + SomeUtils.serverDataToString(serverData));
            }
        }
    }
}