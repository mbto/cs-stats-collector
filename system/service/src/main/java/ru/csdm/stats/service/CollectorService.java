package ru.csdm.stats.service;

import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.SystemEvent;
import ru.csdm.stats.common.dto.MessageQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.utils.SomeUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import static ru.csdm.stats.common.SystemEvent.REFRESH;

@Service
@Lazy(false)
@Slf4j
public class CollectorService {
    @Autowired
    private BlockingQueue<Message<?>> listenerQueue;
    @Autowired
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private Map<String, MessageQueue<Message<?>>> messageQueueByAddress;
    @Autowired
    private Map<Integer, MessageQueue<Message<?>>> messageQueueByQueueId;
//TODO:
//    @Scheduled(fixedDelay = 1 * 60 * 60 * 1000 /* 1h */, initialDelay = 1 * 60 * 60 * 1000 /* 1h */)
//    public void flushOneMapServers() {
//        for (String address : serverDataByAddress.keySet()) {
//            flush(address, FLUSH_FROM_SCHEDULER, true);
//        }
//    }

    public void refresh(UInteger projectId) throws IllegalStateException {
        Message<UInteger> message = new Message<>(null, projectId, REFRESH);
        listenerQueue.add(message);
    }

    public void flush(String address,
                      SystemEvent systemEvent,
                      boolean stopIfRecentlyFlushed) {

        ServerData serverData = serverDataByAddress.get(address);
        if(Objects.isNull(serverData))
            throw new IllegalArgumentException("No serverData found at address '" + address + "'");

        String logMsg;
        if(stopIfRecentlyFlushed) {
            LocalDateTime nextFlushDateTime = serverData.getNextFlushDateTime();
            LocalDateTime now = LocalDateTime.now();

            if(Objects.nonNull(nextFlushDateTime) && nextFlushDateTime.isAfter(now)) {
                logMsg = "Flush " + address + " not available, waiting "
                        + SomeUtils.humanLifetime(nextFlushDateTime, now) + " until next flush";

                serverData.addMessage(logMsg);
                throw new RuntimeException(logMsg);
            }
        }

        MessageQueue<Message<?>> messageQueue = messageQueueByAddress.get(address);
        if(Objects.isNull(messageQueue)) {
            logMsg = "No messageQueue found at address '" + address + "'";

            serverData.addMessage("Flush " + address + " not registered, " + logMsg);
            throw new IllegalArgumentException(logMsg);
        }

        Message<ServerData> message = new Message<>();
        message.setPayload(address);
        message.setPojo(serverData);
        message.setSystemEvent(systemEvent);

        try {
            messageQueue.getMessageQueue().addLast(message);
        } catch (Exception e) {
            serverData.addMessage("Flush " + address + " not registered, " + e.getMessage());

            throw new RuntimeException(e);
        }

        serverData.addMessage("Flush " + address + " registered");
    }
}