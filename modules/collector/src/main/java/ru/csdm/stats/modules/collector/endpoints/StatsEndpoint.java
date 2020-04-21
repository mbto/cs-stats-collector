package ru.csdm.stats.modules.collector.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import ru.csdm.stats.common.dto.DatagramsQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.Player;
import ru.csdm.stats.common.dto.ServerSetting;
import ru.csdm.stats.modules.collector.handlers.DatagramsConsumer;
import ru.csdm.stats.modules.collector.service.SettingsService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
@Slf4j
public class StatsEndpoint {
    @Autowired
    private Map<String, ServerSetting> availableAddresses;
    @Autowired
    private Map<String, Integer> registeredAddresses;
    @Autowired
    private ThreadPoolTaskExecutor applicationTaskExecutor;
    @Autowired
    private ThreadPoolTaskExecutor consumerTaskExecutor;
    @Autowired
    private ThreadPoolTaskExecutor playersSenderTaskExecutor;
    @Autowired
    private Map<Integer, DatagramsQueue> datagramsInQueuesById;
    @Autowired
    private Map<String, Map<String, Player>> gameSessionByAddress;

    @Autowired
    private DatagramsConsumer datagramsConsumer;

/* for tests */
//    @Autowired
//    private ApplicationContext applicationContext;
//    @PostMapping(value = "/quit")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void quit() {
//        int code = SpringApplication.exit(applicationContext, () -> 0);
//        System.exit(code);
//    }

    @PostMapping(value = "/flush")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void flush() {
        LocalDateTime now = LocalDateTime.now();
        for (String address : gameSessionByAddress.keySet()) {
            datagramsConsumer.flushSessions(address, now, "/flush endpoint");
        }
    }

    @Autowired
    private SettingsService settingsService;

    @PostMapping(value = "/updateSettings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSettings() {
        settingsService.updateSettings(false);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("addresses", Arrays.asList(
                Pair.of("available", availableAddresses
                        .values()
                        .stream()
                        .collect(Collectors.groupingBy(ServerSetting::getIpport,
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
                            .collect(Collectors.groupingBy(msg -> msg.getServerSetting().getIpport(),
                                    LinkedHashMap::new,
                                    Collectors.mapping(Message::getPayload, Collectors.toList())));
                }));

        result.put("datagrams in queues", datagramsInQueues);
        result.put("game sessions", gameSessionByAddress);

        return result;
    }

    public Map<String, Integer> buildThreadPoolStats(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("corePoolSize", threadPoolTaskExecutor.getCorePoolSize());
        result.put("maxPoolSize", threadPoolTaskExecutor.getMaxPoolSize());
        result.put("queueSize", threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().size());
        return result;
    }
}