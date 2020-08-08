package ru.csdm.stats.modules.collector.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.DatagramsQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.modules.collector.handlers.DatagramsConsumer;
import ru.csdm.stats.modules.collector.service.SettingsService;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static ru.csdm.stats.common.SystemEvent.FLUSH_SESSIONS;

@RestController
@RequestMapping("/stats")
@Slf4j
public class StatsEndpoint {
    @Autowired
    private ThreadPoolTaskExecutor applicationTaskExecutor;
    @Autowired
    private ThreadPoolTaskExecutor consumerTaskExecutor;
    @Autowired
    private ThreadPoolTaskExecutor playersSenderTaskExecutor;

    @Autowired
    private Map<String, ServerData> availableAddresses;
    @Autowired
    private Map<String, Integer> registeredAddresses;
    @Autowired
    private Map<Integer, DatagramsQueue> datagramsInQueuesById;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Autowired
    private DatagramsConsumer datagramsConsumer;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private CacheManager cacheManager;

    @PostMapping(value = "/flush")
    @PreAuthorize("hasRole('manager')")
    public Map<String, String> flush(Principal principal) {
        log.info("User '" + Optional.ofNullable(principal)
                .map(Principal::getName)
                .orElse("") + "' requested /stats/flush endpoint");

        Map<String, String> results = new LinkedHashMap<>();
        for (Map.Entry<String, ServerData> entry : availableAddresses.entrySet()) {
            String address = entry.getKey();
            Integer queueId = registeredAddresses.get(address);
            if(Objects.isNull(queueId)) {
                results.put(address, "Skipped, due queueId not setted");
                continue;
            }

            DatagramsQueue datagramsQueue = datagramsInQueuesById.get(queueId);
            if(Objects.isNull(datagramsQueue)) {
                results.put(address, "Skipped, due DatagramsQueue not exists");
                continue;
            }

            /* Concurrency template "poison" */
            Message message = new Message();
            message.setServerData(entry.getValue());
            message.setSystemEvent(FLUSH_SESSIONS);

            try {
                datagramsQueue.getDatagramsQueue().addLast(message);
                results.put(address, "Flush registered");
            } catch (Exception e) {
                results.put(address, "Exception, while registration flush. "
                        + ExceptionUtils.getStackTrace(e));
            }
        }

        return results;
    }

    @PostMapping(value = "/updateSettings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('manager')")
    public void updateSettings(Principal principal) {
        log.info("User '" + Optional.ofNullable(principal)
                .map(Principal::getName)
                .orElse("") + "' requested /stats/updateSettings endpoint");

        Optional.ofNullable(cacheManager.getCache("apiUsers")) /* Cache "apiUsers" existed only in "default" profile */
                .ifPresent(Cache::clear);

        settingsService.updateSettings(false);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('manager')")
    public Map<String, Object> stats(Principal principal) {
        log.info("User '" + Optional.ofNullable(principal)
                .map(Principal::getName)
                .orElse("") + "' requested /stats/ endpoint");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("addresses", Arrays.asList(
                Pair.of("available", availableAddresses
                        .values()
                        .stream()
                        .collect(Collectors.groupingBy(ss -> ss.getKnownServer().getIpport(),
                                LinkedHashMap::new,
                                Collectors.toList()))
                ),
                Pair.of("registered with queue id", registeredAddresses)
        ));

        result.put("tasks executors", Arrays.asList(
                Pair.of("application", buildThreadPoolStats(applicationTaskExecutor)),
                Pair.of("consumer", buildThreadPoolStats(consumerTaskExecutor)),
                Pair.of("playersSender", buildThreadPoolStats(playersSenderTaskExecutor))
        ));

        Map<Integer, Map<String, List<String>>> datagramsInQueues = datagramsInQueuesById
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    DatagramsQueue value = entry.getValue();
                    return value.getDatagramsQueue()
                            .stream()
                            .collect(Collectors.groupingBy(msg -> msg.getServerData().getKnownServer().getIpport(),
                                    LinkedHashMap::new,
                                    Collectors.mapping(Message::getPayload, Collectors.toList())));
                }));

        result.put("datagrams in queues", datagramsInQueues);
        result.put("game sessions", gameSessionByAddress);

        return result;
    }

    private Map<String, Integer> buildThreadPoolStats(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("corePoolSize", threadPoolTaskExecutor.getCorePoolSize());
        result.put("maxPoolSize", threadPoolTaskExecutor.getMaxPoolSize());
        result.put("queueSize", threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().size());
        return result;
    }
}