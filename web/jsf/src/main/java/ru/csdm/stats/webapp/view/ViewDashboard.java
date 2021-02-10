package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.Session;
import ru.csdm.stats.service.CollectorService;
import ru.csdm.stats.service.SettingsService;
import ru.csdm.stats.webapp.AggregatedPlayer;
import ru.csdm.stats.webapp.application.ChangesCounter;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static ru.csdm.stats.common.SystemEvent.FLUSH_FROM_ENDPOINT;

@ViewScoped
@Named
@Slf4j
public class ViewDashboard {
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private CollectorService collectorService;
    @Autowired
    private ChangesCounter changesCounter;

    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Getter
    private Map<String, List<AggregatedPlayer>> aggregatedByAddress;

    public void updateSettings() {
        log.info("Manager requested updateSettings()");

        settingsService.updateSettings(false);
        changesCounter.getCounter().set(0);
    }

    public Map<String, String> flush() {
        log.info("Manager requested flush()");

        Map<String, String> flush = collectorService.flush(FLUSH_FROM_ENDPOINT, false);

        changesCounter.getCounter().set(0);

        return flush;
    }

    /*public void stats() throws Exception {
        log.info("Manager requested stats()");
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("addresses", Arrays.asList(
                Pair.of("available", availableAddresses),
                Pair.of("registered with queue id", registeredAddresses)
        ));

        Map<Integer, Map<String, List<String>>> datagramsInQueues = datagramsInQueuesById
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    DatagramsQueue value = entry.getValue();
                    return value.getDatagramsQueue()
                            .stream()
                            .collect(Collectors.groupingBy(message -> message.getServerData().getKnownServer().getIpport(),
                                    LinkedHashMap::new,
                                    Collectors.mapping(Message::getPayload, Collectors.toList())));
                }));

        result.put("datagrams in queues", datagramsInQueues);
    }*/

    public void fetch() {
        aggregatedByAddress = gameSessionByAddress
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, gameSessionsEntry -> {
                    Map<String, CollectedPlayer> gameSessions = gameSessionsEntry.getValue();

                    return gameSessions.entrySet()
                            .stream()
                            .filter(e -> !e.getValue().getSessions().isEmpty())
                            .map(collectedPlayerEntry -> {
                                String name = collectedPlayerEntry.getKey();
                                CollectedPlayer collectedPlayer = collectedPlayerEntry.getValue();

                                AggregatedPlayer aggPlayer = new AggregatedPlayer();
                                for (Session session : collectedPlayer.getSessions()) {
                                    aggPlayer.setName(name);
                                    aggPlayer.setKills(session.getKills());
                                    aggPlayer.setDeaths(session.getDeaths());
                                    aggPlayer.setStarted(session.getStarted());
                                    aggPlayer.setFinished(session.getFinished());
                                }

                                if(!collectedPlayer.getIpAddresses().isEmpty()) {
                                    Set<SelectItem> ips = collectedPlayer.getIpAddresses()
                                            .stream()
                                            .map(value -> new SelectItem(value, value))
                                            .collect(Collectors.toSet());
                                    aggPlayer.setIpAddresses(ips);
                                }

                                if(!collectedPlayer.getSteamIds().isEmpty()) {
                                    Set<SelectItem> steamIds = collectedPlayer.getSteamIds()
                                            .stream()
                                            .map(value -> new SelectItem(value, value))
                                            .collect(Collectors.toSet());
                                    aggPlayer.setSteamIds(steamIds);
                                }

                                return aggPlayer;
                            }).collect(Collectors.toList());
                }));
    }

    public int getCorePoolSize(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        return threadPoolTaskExecutor.getCorePoolSize();
    }
    public int getMaxPoolSize(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        return threadPoolTaskExecutor.getMaxPoolSize();
    }
    public int getQueueSize(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        return threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().size();
    }
    public String group(Collection<String> list) {
        return String.join("<br/>", list);
    }
}