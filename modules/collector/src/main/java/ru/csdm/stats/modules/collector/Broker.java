package ru.csdm.stats.modules.collector;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.SystemEvent;
import ru.csdm.stats.common.dto.*;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.dao.CollectorDao;
import ru.csdm.stats.modules.collector.handlers.DatagramsConsumer;
import ru.csdm.stats.modules.collector.settings.PacketUtils;
import ru.csdm.stats.service.InstanceHolder;

import javax.annotation.PreDestroy;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static ru.csdm.stats.common.SystemEvent.*;
import static ru.csdm.stats.common.SystemEvent.CONSUME_DATAGRAM;
import static ru.csdm.stats.common.utils.SomeUtils.addressToString;

@Service
@Lazy(false)
@Slf4j
public class Broker {
    @Autowired
    private BlockingDeque<Message<?>> brokerQueue;
    @Autowired
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private Map<String, MessageQueue> messageQueueByAddress;
    @Autowired
    private Map<Integer, MessageQueue> messageQueueByQueueId;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Autowired
    private ThreadPoolTaskExecutor consumerTE;

    @Autowired
    private DatagramsConsumer datagramsConsumer;
    @Autowired
    private CollectorDao collectorDao;
    @Autowired
    private InstanceHolder instanceHolder;
    @Setter
    private DatagramSocket datagramSocket;

    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();

    @PreDestroy
    public void destroy() {
        if(log.isDebugEnabled())
            log.debug("destroy() start");

        if(Objects.nonNull(datagramSocket)) {
            try {
                datagramSocket.close();
            } catch (Throwable ignored) { }
        }

        if(log.isDebugEnabled())
            log.debug("destroy() end");
    }

    @Async("brokerTE")
    public void launchReceiverAsync() {
        log.info("Activating receiver at " + addressToString(datagramSocket.getLocalSocketAddress()));

        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

        while (true) {
            if (datagramSocket.isClosed()) {
                log.info("Deactivation receiver detected");
                break;
            }

            try {
                datagramSocket.receive(packet);
            } catch (Throwable e) {
                if (datagramSocket.isClosed()) {
                    log.info("Deactivation receiver detected");
                    break;
                }

                log.warn("Exception while receive datagram packet", e);
                continue;
            }

            //todo: change to port
            String address = addressToString(packet.getSocketAddress());
            Message<DatagramPacket> message = new Message<>(address, packet, CONSUME_DATAGRAM);

            putLastWithTryes(brokerQueue, message);
        }

        putLastWithTryes(brokerQueue, new Message<>(null, null, FLUSH_AND_QUIT));

        log.info("Deactivated receiver");
    }

    @Async("brokerTE")
    public void launchDistributorAsync() {
        log.info("Activating distributor");

        Message<?> message;

        while (true) {
            try {
                message = brokerQueue.takeFirst();
            } catch (Throwable e) {
                log.warn("Exception while takeFirst message", e);
                continue;
            }

            SystemEvent systemEvent = message.getSystemEvent();

            if(systemEvent == CONSUME_DATAGRAM) {
                consumeDatagram(message);
            } else if(systemEvent == REFRESH) {
                consumeRefreshEvent(message);
            } else if(systemEvent == FLUSH_FROM_FRONTEND || systemEvent == FLUSH_FROM_SCHEDULER) {
                consumeFlush(message);
            } else if(systemEvent == FLUSH_AND_QUIT) {
                consumeFlushAndQuit(message);

                break;
            } else {
                log.warn("Unknown system event '" + systemEvent + "'");
            }
        }

        log.info("Deactivated distributor");
    }

    private void consumeDatagram(Message<?> originalMessage) {
        String address = originalMessage.getPayload();
        ServerData serverData = serverDataByAddress.get(address);

        if(serverData == null || !serverData.isKnownServerActive())
            return;

        DatagramPacket packet = (DatagramPacket) originalMessage.getPojo();

        if(!PacketUtils.validate("CS16", address, packet))
            return;

        Message<ServerData> message = new Message<>(
                PacketUtils.convert("CS16", address, packet),
                serverData,
                null);

        MessageQueue messageQueue = messageQueueByAddress.get(address);

        if(log.isDebugEnabled())
            log.debug(address + " Sending to " + messageQueue + " message: " + message);

        putLastWithTryes(messageQueue, message);
    }

    private void consumeRefreshEvent(Message<?> originalMessage) {
        log.info("Started synchronization by system event " + originalMessage.getSystemEvent());

        Collection<MessageQueue> messageQueues = messageQueueByQueueId.values();
        CyclicBarrier cb = new CyclicBarrier(messageQueues.size() + 1,
                () -> refresh((UInteger) originalMessage.getPojo()));

        Message<CyclicBarrier> message = new Message<>(null, cb, originalMessage.getSystemEvent());

        for (MessageQueue messageQueue : messageQueues) {
            if (!putLastWithTryes(messageQueue, message)) {
                break;
            }
        }

        if(cb.isBroken()) {
            log.warn("Failed synchronization");
            return;
        }

        log.info("Waiting synchronization");

        try {
            cb.await(5, TimeUnit.SECONDS);
        } catch (Throwable e) {
            log.warn("Exception while await synchronization", e);
            return;
        }

        log.info("Finished synchronization");
    }

    private void consumeFlush(Message message) {
        String address = message.getPayload();
        ServerData expectedServerData = (ServerData) message.getPojo();
        ServerData serverData = serverDataByAddress.get(address);

        if(Objects.isNull(serverData)) // serverData & gameSessions already deleted if refreshed
            return;

        // if flush from frontend or scheduler after refresh - registry might already be modified
        if(Objects.nonNull(expectedServerData) &&
                !serverData.getProject().getId().equals(expectedServerData.getProject().getId())) {

            String logMsg = "Skip flushing players";

            Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(address);
            if(Objects.nonNull(gameSessions)) {
                int sessionsCount = 0;
                for (CollectedPlayer collectedPlayer : gameSessions.values()) {
                    List<Session> sessions = collectedPlayer.getSessions();
                    sessionsCount += sessions.size();
                    sessions.clear();
                }

                logMsg += " & removed " + gameSessions.size() + " players (" + sessionsCount + " sessions)";
            }

            logMsg += ", due different project ids after refresh registry";
            log.info(address + " " + logMsg);
            serverData.addMessage(logMsg);
            return;
        }

        message.setPojo(serverData); // without creating new Message

        putLastWithTryes(messageQueueByAddress.get(address), message);
    }

    private void consumeFlushAndQuit(Message<?> message) {
        log.info("Deactivation distributor detected");

        for (Map.Entry<Integer, MessageQueue> entry : messageQueueByQueueId.entrySet()) {
            MessageQueue messageQueue = entry.getValue();
            putLastWithTryes(messageQueue, message);
        }
    }

    void refresh(UInteger projectId) {
        Instance instance = instanceHolder.getAvailableInstances()
                .get(instanceHolder.getCurrentInstanceId());

        log.info("Updating servers settings from database, instance: "
                + "[" + instance.getId() + "] "
                + instance.getName()
                + (StringUtils.isNotBlank(instance.getDescription()) ? " (" + instance.getDescription() + ")" : ""));

        CollectorData collectorDataSlice;
        try {
            // Slice of "now data"
            collectorDataSlice = collectorDao.fetchCollectorData(instanceHolder.getCurrentInstanceId(), projectId);
        } catch (Throwable e) {
            throw new RuntimeException("Unable to fetch collector data from database", e);
        }

        List<KnownServer> knownServersSlice = collectorDataSlice.getKnownServers();
        Map<UInteger, Project> projectByProjectId = collectorDataSlice.getProjectByProjectId();
        Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId = collectorDataSlice.getDriverPropertiesByProjectId();

        LocalDateTime now = LocalDateTime.now();
/*  registry:
    127.0.0.1:27015 - ServerData proj1
    127.0.0.1:27016 - ServerData proj1
    127.0.0.1:27017 - ServerData proj2
    127.0.0.1:27018 - ServerData proj2
    127.0.0.1:27019 - ServerData proj2

    knownServersSlice: instance + projectId
    127.0.0.1:27015 proj1
    127.0.0.1:27016 proj1
    127.0.0.1:27017 proj1

    knownServersSlice: instance
    127.0.0.1:27015 proj1
    127.0.0.1:27016 proj1
    127.0.0.1:27017 proj1
    127.0.0.1:27018 proj2
*/
        // search noneMatches (removed) -> remove
        Iterator<Map.Entry<String, ServerData>> serverDataByAddressIterator = serverDataByAddress.entrySet().iterator();
        while (serverDataByAddressIterator.hasNext()) {
            Map.Entry<String, ServerData> entry = serverDataByAddressIterator.next();
            String address = entry.getKey();
            ServerData serverData = entry.getValue();

            //ignoring other projects if filter by projectId exists
            if(Objects.nonNull(projectId) && !serverData.getProject().getId().equals(projectId))
                continue;

            boolean noneMatches = knownServersSlice
                    .stream()
                    .noneMatch(knownServer -> knownServer.getIpport().equals(address));

            if(noneMatches) {
                serverDataByAddressIterator.remove(); // remove relationship from registry

                MessageQueue messageQueue = messageQueueByAddress.remove(address);
                if(Objects.nonNull(messageQueue) && serverData.isKnownServerActive()) {
                    messageQueue.removePort(address);
                }

                String logMsg = "Remove: ";

                Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(address);
                if(Objects.nonNull(gameSessions)) {
                    int sessionsCount = 0;
                    for (CollectedPlayer collectedPlayer : gameSessions.values()) {
                        List<Session> sessions = collectedPlayer.getSessions();
                        sessionsCount += sessions.size();
                        sessions.clear();
                    }
                    logMsg += gameSessions.size() + " players (" + sessionsCount + " sessions)";

                    /* clear & remove gameSessions registry, without flush */
                    gameSessions.clear();
                    gameSessionByAddress.remove(address);
                }

                log.info(address + " " + logMsg);
            }
        }

        Map<Boolean, List<KnownServer>> partitioned = knownServersSlice
                .stream()
                .collect(Collectors.partitioningBy(
                        knownServer -> serverDataByAddress.containsKey(knownServer.getIpport())));

        for (Map.Entry<Boolean, List<KnownServer>> entry : partitioned.entrySet()) {
            if(entry.getKey()) { // exists -> update & replace
                for (KnownServer newKnownServer : entry.getValue()) {
                    String address = newKnownServer.getIpport();
                    ServerData currentServerData = serverDataByAddress.get(address);

                    ServerData newServerData = new ServerData();
                    newServerData.setKnownServer(newKnownServer);
                    newServerData.setProject(projectByProjectId.get(newKnownServer.getProjectId()));
                    newServerData.setNextFlushDateTime(currentServerData.getNextFlushDateTime());
                    newServerData.setDriverProperties(driverPropertiesByProjectId.get(newKnownServer.getProjectId()));

                    newServerData.setLastTouchDateTime(currentServerData.getLastTouchDateTime());
                    newServerData.setMessages(currentServerData.getMessages());

                    serverDataByAddress.replace(address, newServerData);

                    //todo: переписать, чтобы неудалялся, если выбрана таже очередь
                    //+ разделить поиск лучшей очереди на 2 метода - поиска и создания
                    //возможно придётся очередям присваивать UUID, тк могут создасться
                    //с такимже номером, которые удалились недавно
                    MessageQueue currentMessageQueue = messageQueueByAddress.remove(address); // remove relationship from registry
                    boolean isCurrentKnownServerActive = Objects.nonNull(currentMessageQueue)
                            && currentServerData.isKnownServerActive();
                    if(isCurrentKnownServerActive)
                        currentMessageQueue.removePort(address);

                    String logMsg = "Update: listening ";
                    if(newServerData.isKnownServerActive()) {
                        MessageQueue newMessageQueue = allocateMessageQueue();
                        newMessageQueue.addPort(address);
                        messageQueueByAddress.put(address, newMessageQueue);

                        if(isCurrentKnownServerActive) {
                            logMsg += "refreshed: " + currentMessageQueue + " -> " + newMessageQueue;
                        } else {
                            logMsg += "started at " + newMessageQueue;
                        }
                    } else {
                        if(isCurrentKnownServerActive) {
                            logMsg += "stopped in " + currentMessageQueue;
                        } else {
                            logMsg += "already stopped";
                        }
                    }

                    log.info(address + " " + logMsg);
                    newServerData.addMessage(logMsg);
                }
            } else { // not exists -> insert
                for (KnownServer knownServer : entry.getValue()) {
                    String address = knownServer.getIpport();

                    ServerData serverData = new ServerData();
                    serverData.setKnownServer(knownServer);
                    serverData.setProject(projectByProjectId.get(knownServer.getProjectId()));
                    serverData.setNextFlushDateTime(now.plusHours(1));
                    serverData.setDriverProperties(driverPropertiesByProjectId.get(knownServer.getProjectId()));

                    serverData.setLastTouchDateTime(now);

                    serverDataByAddress.put(address, serverData);

                    String logMsg = "New: ";
                    if(serverData.isKnownServerActive()) {
                        MessageQueue messageQueue = allocateMessageQueue();
                        messageQueue.addPort(address);
                        messageQueueByAddress.put(address, messageQueue);

                        logMsg += "listening started at " + messageQueue;
                    } else {
                        logMsg += "added to registry";
                    }

                    log.info(address + " " + logMsg);
                    serverData.addMessage(logMsg);
                }
            }
        }

        if(!messageQueueByQueueId.isEmpty()) {
            Message<?> message = new Message<>(null, null, QUIT);

            Iterator<MessageQueue> messageQueueIterator = messageQueueByQueueId.values().iterator();
            while (messageQueueIterator.hasNext()) {
                MessageQueue messageQueue = messageQueueIterator.next();

                if (messageQueue.getKnownServersPorts().isEmpty()) { // without relationships
                    log.info("Removing " + messageQueue);
                    messageQueueIterator.remove(); // remove MessageQueue from registry

                    putLastWithTryes(messageQueue, message); // send QUIT event in empty queue
                }
            }
        }

        int newPoolSize = messageQueueByQueueId.size();

        int oldPoolSize = consumerTE.getCorePoolSize();
        if(newPoolSize > oldPoolSize) {
            consumerTE.setMaxPoolSize(newPoolSize != 0 ? newPoolSize : 1);
            consumerTE.setCorePoolSize(newPoolSize);
        } else if(newPoolSize < oldPoolSize) {
            consumerTE.setCorePoolSize(newPoolSize);
            consumerTE.setMaxPoolSize(newPoolSize != 0 ? newPoolSize : 1);
        }

        if(newPoolSize != oldPoolSize)
            log.info("Changing consumerTE pool size from " + oldPoolSize + " to " + newPoolSize + "");

        int size = serverDataByAddress.size();
        log.info("Registry serverData: " + size + " relationship" + (size > 1 ? "s" : ""));

        if(size > 0) {
            for (ServerData serverData : serverDataByAddress.values()) {
                String address = serverData.getKnownServer().getIpport();
                log.info(serverData.toString() + (serverData.isKnownServerActive()
                        ? ", " + messageQueueByAddress.get(address) : ""));
            }
        }
    }

    private MessageQueue allocateMessageQueue() {
        int size = messageQueueByQueueId.size();
        if(size < availableProcessors) {
            // add
            int queueId = size + 1;
            MessageQueue messageQueue = new MessageQueue(queueId);
            log.info("Created " + messageQueue);

            messageQueueByQueueId.put(queueId, messageQueue);
            datagramsConsumer.startConsumeAsync(messageQueue);
            return messageQueue;
        }

        // search by min(messageQueue.knownServersPorts.size)
        return messageQueueByQueueId
                .values()
                .stream()
                .min(Comparator.comparingInt(mq -> mq.getKnownServersPorts().size()))
                .orElseThrow(IllegalStateException::new);
    }

    private boolean putLastWithTryes(MessageQueue messageQueue, Message<?> message) {
        return putLastWithTryes(messageQueue.getMessageQueue(), message);
    }

    private boolean putLastWithTryes(BlockingDeque<Message<?>> queue, Message<?> message) {
        int tryes = 0;
        while (true) {
            try {
                ++tryes;
                queue.putLast(message);
                return true;
            } catch (Throwable e) {
                log.warn("Exception while putLast message " + message + " in queue, " + tryes + "/3");

                if (tryes == 3) {
                    log.warn("Failed putLast message " + message + " in queue");
                    return false;
                }
            }
        }
    }
}