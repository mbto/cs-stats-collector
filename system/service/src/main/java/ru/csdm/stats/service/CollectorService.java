package ru.csdm.stats.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.SystemEvent;
import ru.csdm.stats.common.dto.DatagramsQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.utils.SomeUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

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
//TODO:
//    @Scheduled(fixedDelay = 1 * 60 * 60 * 1000 /* 1h */, initialDelay = 1 * 60 * 60 * 1000 /* 1h */)
//    public void flushOneMapServers() {
//        Map<String, String> results = flush(FLUSH_FROM_SCHEDULER, true);
//
//        StringBuilder sb = new StringBuilder("Autoflush results\n");
//
//        for (Map.Entry<String, String> entry : results.entrySet()) {
//            sb.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
//        }
//
//        log.info(sb.toString());
//    }

    public void flush(String address,
                      SystemEvent systemEvent,
                      boolean skipRecentlyFlushed) {

        ServerData serverData = availableAddresses.get(address);
        if(Objects.isNull(serverData))
            throw new IllegalArgumentException("No serverData found at address '" + address + "'");

        String logMsg;
        Integer queueId = registeredAddresses.get(address);
        if(Objects.isNull(queueId)) {
            logMsg = "No queueId found at address '" + address + "'";
            serverData.addMessage(logMsg);

            throw new IllegalArgumentException(logMsg);
        }

        if(skipRecentlyFlushed) {
            LocalDateTime nextFlushDateTime = serverData.getNextFlushDateTime();
            LocalDateTime now = LocalDateTime.now();

            if(Objects.nonNull(nextFlushDateTime) && nextFlushDateTime.isAfter(now)) {
                logMsg = "Flush " + address + " not available, waiting "
                        + SomeUtils.humanLifetime(nextFlushDateTime, now) + " until next flush";

                serverData.addMessage(logMsg);
                throw new RuntimeException(logMsg);
            }
        }

        DatagramsQueue datagramsQueue = datagramsInQueuesById.get(queueId);
        if(Objects.isNull(datagramsQueue)) {
            logMsg = "No datagramsQueue found at address '"
                    + address + "' queueId '" + queueId + "'";

            serverData.addMessage(logMsg);
            throw new IllegalArgumentException(logMsg);
        }

        Message message = new Message();
        message.setServerData(serverData);
        message.setSystemEvent(systemEvent);

        try {
            datagramsQueue.getDatagramsQueue().addLast(message);
        } catch (Exception e) {
            serverData.addMessage("Flush " + address + " not registered, " + e.getMessage());

            throw new RuntimeException(e);
        }

        serverData.addMessage("Flush " + address + " registered");
    }
}