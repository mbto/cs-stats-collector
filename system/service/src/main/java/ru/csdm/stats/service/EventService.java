package ru.csdm.stats.service;

import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.BrokerEvent;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.MessageQueue;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.utils.SomeUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;

import static ru.csdm.stats.common.BrokerEvent.REFRESH;

@Service
@Lazy(false)
@Slf4j
public class EventService {
    @Autowired
    private BlockingDeque<Message<?>> brokerQueue;
    @Autowired
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private Map<String, MessageQueue> messageQueueByAddress;
//    @Autowired
//    private Map<Integer, MessageQueue> messageQueueByQueueId;
//TODO:
//    @Scheduled(fixedDelay = 1 * 60 * 60 * 1000 /* 1h */, initialDelay = 1 * 60 * 60 * 1000 /* 1h */)
//    public void flushOneMapServers() {
//        for (String address : serverDataByAddress.keySet()) {
//            flush(address, FLUSH_FROM_SCHEDULER, true);
//        }
//    }

    /**
     * Refresh changes in registries
     */
    public void refreshSettings(UInteger projectId) throws IllegalStateException {
        Message<UInteger> message = new Message<>(null, projectId, REFRESH);
        brokerQueue.addFirst(message);
    }

    public void flushSessions(String port,
                              BrokerEvent brokerEvent,
                              boolean stopIfRecentlyFlushed) {

        ServerData serverData = serverDataByAddress.get(port);
        if(Objects.isNull(serverData))
            throw new IllegalArgumentException("No serverData found at port '" + port + "'");

        // this case only for 'dev' environment, due messageQueueByAddress can be filled manually with ViewDashboard.makeFakes()
        MessageQueue messageQueue = messageQueueByAddress.get(port);
        if(Objects.isNull(messageQueue))
            throw new IllegalArgumentException("No messageQueue found at port '" + port + "'");

        String logMsg;
        if(stopIfRecentlyFlushed) {
            LocalDateTime nextFlushDateTime = serverData.getNextFlushDateTime();
            LocalDateTime now;

            if(Objects.nonNull(nextFlushDateTime) && nextFlushDateTime.isAfter(now = LocalDateTime.now())) {
                logMsg = "Flush " + port + " not available, waiting "
                        + SomeUtils.humanLifetime(nextFlushDateTime, now) + " until next flush";

                serverData.addMessage(logMsg);
                throw new RuntimeException(logMsg);
            }
        }

        Message<ServerData> message = new Message<>(port, serverData, brokerEvent);

        try {
            brokerQueue.addFirst(message);
        } catch (Throwable e) {
            serverData.addMessage("Flush " + port + " not registered, " + e.getMessage());

            throw new RuntimeException(e);
        }

        serverData.addMessage("Flush " + port + " registered");
    }
}