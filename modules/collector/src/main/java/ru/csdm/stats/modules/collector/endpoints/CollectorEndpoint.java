package ru.csdm.stats.modules.collector.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
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
import ru.csdm.stats.modules.collector.service.CollectorService;
import ru.csdm.stats.modules.collector.service.SettingsService;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static ru.csdm.stats.common.SystemEvent.FLUSH_FROM_ENDPOINT;

@RestController
@RequestMapping("/stats") //todo: will be removed in favor frontend
@Slf4j
public class CollectorEndpoint {
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
    private CollectorService collectorService;

    @Autowired
    private SettingsService settingsService;

    @PostMapping(value = "/flush")
    public Map<String, String> flush(Principal principal) {
        log.info("Manager '" + Optional.ofNullable(principal)
                .map(Principal::getName)
                .orElse("") + "' requested /stats/flush");

        return collectorService.flush(FLUSH_FROM_ENDPOINT, false);
    }

    @PostMapping(value = "/updateSettings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSettings(Principal principal) {
        log.info("Manager '" + Optional.ofNullable(principal)
                .map(Principal::getName)
                .orElse("") + "' requested /stats/updateSettings");

        settingsService.updateSettings(false);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> stats(Principal principal) {
        log.info("Manager '" + Optional.ofNullable(principal)
                .map(Principal::getName)
                .orElse("") + "' requested /stats/");

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("addresses", Arrays.asList(
                Pair.of("available", availableAddresses),
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
                            .collect(Collectors.groupingBy(message -> message.getServerData().getKnownServer().getIpport(),
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