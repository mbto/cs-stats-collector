package ru.csdm.stats.modules.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.SystemEvent;
import ru.csdm.stats.common.dto.DatagramsQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.utils.SomeUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static ru.csdm.stats.common.SystemEvent.FLUSH_FROM_SCHEDULER;

@Service
@Lazy(false)
@Slf4j
public class CollectorService {
    @Autowired
    private Map<String, ServerData> availableAddresses;
    @Autowired
    private Map<String, Integer> registeredAddresses;
    @Autowired
    private Map<Integer, DatagramsQueue> datagramsInQueuesById;

    @Scheduled(fixedDelay = 1 * 60 * 60 * 1000 /* 1h */, initialDelay = 1 * 60 * 60 * 1000 /* 1h */)
    public void flushOneMapServers() {
        Map<String, String> results = flush(FLUSH_FROM_SCHEDULER, true);

        StringBuilder sb = new StringBuilder("Autoflush results\n");

        for (Map.Entry<String, String> entry : results.entrySet()) {
            sb.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
        }

        log.info(sb.toString());
    }

    public Map<String, String> flush(SystemEvent systemEvent, boolean skipRecentlyFlushed) {
        Map<String, String> results = new LinkedHashMap<>();

        LocalDateTime now = LocalDateTime.now();

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

            ServerData serverData = entry.getValue();

            if(skipRecentlyFlushed) {
                LocalDateTime nextFlushDateTime = serverData.getNextFlushDateTime();

                if(Objects.nonNull(nextFlushDateTime)) {
                    if(nextFlushDateTime.isAfter(now)) {
                        results.put(address, "Skipped, due waiting "
                                + SomeUtils.humanLifetime(nextFlushDateTime, now) + " until next flush");

                        continue;
                    }
                }
            }

            Message message = new Message();
            message.setServerData(serverData);
            message.setSystemEvent(systemEvent);

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
}