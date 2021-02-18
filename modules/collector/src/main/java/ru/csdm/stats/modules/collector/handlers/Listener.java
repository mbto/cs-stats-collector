package ru.csdm.stats.modules.collector.handlers;

import lombok.Getter;
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
import ru.csdm.stats.modules.collector.settings.PacketUtils;
import ru.csdm.stats.service.InstanceHolder;

import javax.annotation.PreDestroy;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.csdm.stats.common.SystemEvent.*;
import static ru.csdm.stats.common.SystemEvent.CONSUME_DATAGRAM;
import static ru.csdm.stats.common.SystemEvent.CONSUME_GAMELOG;
import static ru.csdm.stats.common.utils.SomeUtils.addressToString;

@Service
@Lazy(false)
@Slf4j
public class Listener {
    @Autowired
    private BlockingQueue<Message<?>> listenerQueue;
    @Autowired
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private Map<String, MessageQueue<Message<?>>> messageQueueByAddress;
    @Autowired
    private Map<Integer, MessageQueue<Message<?>>> messageQueueByQueueId;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Autowired
    private ThreadPoolTaskExecutor consumerTaskExecutor;

    @Autowired
    private DatagramsConsumer datagramsConsumer;
    @Autowired
    private CollectorDao collectorDao;
    @Autowired
    private InstanceHolder instanceHolder;
    @Setter
    private DatagramSocket datagramSocket;

    @Getter
    @Setter /* Setter - allowing calling from another class/thread, with spring proxy, without volatile */
    private boolean deactivated;

    @PreDestroy
    public void destroy() {
        if(log.isDebugEnabled())
            log.debug("destroy() start");

        setDeactivated(true);

        if(Objects.nonNull(datagramSocket)) {
            try {
                datagramSocket.close();
            } catch (Throwable ignored) { }
        }

        if(log.isDebugEnabled())
            log.debug("destroy() end");
    }

    public void setupConsumers() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        log.info("Setup consumer" + (availableProcessors > 1 ? "s" : "")
                + ": available " + availableProcessors + " processor"
                + (availableProcessors > 1 ? "s" : ""));

        int[] queueIds = IntStream.range(0, availableProcessors)
                .map(num -> num + 1)
                .toArray();

        log.info("Creating " + queueIds.length
                + " MessageQueue" + (queueIds.length > 1 ? "s" : "")
                + " with id" + (queueIds.length > 1 ? "s" : "")
                + " # " + Arrays.toString(queueIds));

        for (int queueId : queueIds) {
            MessageQueue<Message<?>> messageQueue = new MessageQueue<>(queueId);
            messageQueueByQueueId.put(queueId, messageQueue);

            datagramsConsumer.startConsumeAsync(messageQueue);
        }
    }

    @Async("coreExecutor")
    public void launchReceiverAsync() {
        log.info("Receiver started at " + addressToString(datagramSocket.getLocalSocketAddress()));

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (true) {
            if (isDeactivated()) {
                log.info("Deactivation detected");
                break;
            }

            try {
                datagramSocket.receive(packet);
                //todo: debug for search another concat method
                String address = addressToString(packet.getSocketAddress());

                Message<DatagramPacket> message = new Message<>(address, packet, CONSUME_DATAGRAM);

                listenerQueue.put(message); // todo:review? move to another catch block? or remove comment
            } catch (Throwable e) {
                if (isDeactivated()) {
                    log.info("Deactivation detected");
                    break;
                }

                log.warn("Exception while receiving/sending datagram packet", e);
                continue;
            }
        }

        log.info("Receiver deactivated");
    }

    @Async("coreExecutor")
    public void launchDistributorAsync() {
        log.info("Distributor started");

        Message<?> originalMessage;

        while (true) {
            if (isDeactivated()) {
                log.info("Deactivation detected");
                break;
            }

            try {
                originalMessage = listenerQueue.take();
            } catch (Throwable e) {
                if (isDeactivated()) {
                    log.info("Deactivation detected");
                    break;
                }

                log.warn("Exception while taking datagram packet", e);
                continue;
            }

            SystemEvent systemEvent = originalMessage.getSystemEvent();
            if(systemEvent == CONSUME_DATAGRAM) {
                consumeDatagram(originalMessage);
            } else if(systemEvent == REFRESH) {
                consumeRefreshEvent(originalMessage);
            } else {
                log.warn("Unknown system event '" + systemEvent + "'");
            }
        }

        log.info("Distributor deactivated");
    }

    private void consumeDatagram(Message<?> originalMessage) {
        String address = originalMessage.getPayload();
        ServerData serverData = serverDataByAddress.get(address);

        if(serverData == null || !serverData.isListening())
            return;

        DatagramPacket packet = (DatagramPacket) originalMessage.getPojo();

        if(!PacketUtils.validate("CS16", address, packet))
            return;

        Message<ServerData> message = new Message<>();
        message.setPojo(serverData);
        message.setPayload(PacketUtils.convert("CS16", address, packet));
        message.setSystemEvent(CONSUME_GAMELOG);

        MessageQueue<Message<?>> messageQueue = messageQueueByAddress.get(address);

        if(log.isDebugEnabled())
            log.debug(address + " Sending to " + messageQueue + " message: " + message);

        putLastWithTryes(messageQueue, message);
    }

    private void consumeRefreshEvent(Message<?> originalMessage) {
        log.info("Started synchronization by system event " + originalMessage.getSystemEvent());

        Collection<MessageQueue<Message<?>>> messageQueues = messageQueueByQueueId.values();
        CyclicBarrier cb = new CyclicBarrier(messageQueues.size() + 1,
                () -> refresh((UInteger) originalMessage.getPojo()));

        Message<CyclicBarrier> message = new Message<>(null, cb, originalMessage.getSystemEvent());

        for (MessageQueue<Message<?>> messageQueue : messageQueues) {
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
            if (isDeactivated()) {
                log.info("Deactivation detected after await synchronization");
                return;
            }

            log.warn("Exception after await synchronization", e);
            return;
        }

        log.info("Finished synchronization");
    }

    private boolean putLastWithTryes(MessageQueue<Message<?>> messageQueue, Message<?> message) {
        int tryes = 0;
        while (true) {
            try {
                ++tryes;
                messageQueue.getMessageQueue().putLast(message);
                return true;
            } catch (Throwable e) {
                if(isDeactivated())
                    return false;

                log.warn("Exception, while putting message " + message + " in " + messageQueue + ", " + tryes + "/3");

                if (tryes == 3) {
                    log.warn("Failed put message " + message + " in " + messageQueue);
                    return false;
                }
            }
        }
    }

    public void refresh(UInteger projectId) {
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
        Iterator<Map.Entry<String, ServerData>> iterator = serverDataByAddress.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServerData> entry = iterator.next();
            String address = entry.getKey();
            ServerData serverData = entry.getValue();

            //ignoring other projects if projectId exists
            if(Objects.nonNull(projectId) && !serverData.getProject().getId().equals(projectId))
                continue;

            boolean noneMatches = knownServersSlice
                    .stream()
                    .noneMatch(knownServer -> knownServer.getIpport().equals(address));

            if(noneMatches) {
                iterator.remove();

                MessageQueue<Message<?>> messageQueue = messageQueueByAddress.remove(address);
                if(Objects.nonNull(messageQueue) && serverData.isListening()) {
                    messageQueue.decListening();
                }

                String logMsg = "removed from listener";

                Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(address);
                if(Objects.nonNull(gameSessions)) {
                    logMsg += ", " + gameSessions.size() + " gameSessions removed";

                    /* clear & remove gameSessions container, without flush */
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
                    newServerData.setNextFlushDateTime(now.plusHours(1));
                    newServerData.setDriverProperties(driverPropertiesByProjectId.get(newKnownServer.getProjectId()));

                    newServerData.setLastTouchDateTime(currentServerData.getLastTouchDateTime());
                    newServerData.setMessages(currentServerData.getMessages());

                    MessageQueue<Message<?>> currentMessageQueue = messageQueueByAddress.get(address);
                    boolean currentIsListening = Objects.nonNull(currentMessageQueue) && currentServerData.isListening();
                    if(currentIsListening)
                        currentMessageQueue.decListening();

                    MessageQueue<Message<?>> newMessageQueue = findOptimalMessageQueue();

                    messageQueueByAddress.replace(address, newMessageQueue);
                    serverDataByAddress.replace(address, newServerData);

                    String logMsg = "listening ";
                    if(newServerData.isListening()) {
                        newMessageQueue.incListening();

                        if(currentIsListening) {
                            logMsg += "refreshed";
                        } else {
                            logMsg += "started";
                        }
                    } else {
                        if(currentIsListening) {
                            logMsg += "stopped";
                        } else {
                            logMsg += "stopped again";
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

                    MessageQueue<Message<?>> messageQueue = findOptimalMessageQueue();;

                    messageQueueByAddress.put(address, messageQueue);
                    serverDataByAddress.put(address, serverData);

                    String logMsg;
                    if(serverData.isListening()) {
                        messageQueue.incListening();

                        logMsg = "listening started";
                    } else {
                        logMsg = "added to listener";
                    }

                    log.info(address + " " + logMsg);
                    serverData.addMessage(logMsg);
                }
            }
        }

        //todo: найти динамически незадействованные MessageQueue
        // (если 64 процессора - нам не нужно создавать все 64 MessageQueue на старте приложения)

        int countListening = (int) serverDataByAddress.values()
                .stream()
                .filter(ServerData::isListening)
                .count();

        int newPoolSize = Math.max(1,
                Math.min(countListening, messageQueueByAddress.size())
        );

        int oldPoolSize = consumerTaskExecutor.getCorePoolSize();
        if(newPoolSize > oldPoolSize) {
            consumerTaskExecutor.setMaxPoolSize(newPoolSize);
            consumerTaskExecutor.setCorePoolSize(newPoolSize);
        } else if(newPoolSize < oldPoolSize) {
            consumerTaskExecutor.setCorePoolSize(newPoolSize);
            consumerTaskExecutor.setMaxPoolSize(newPoolSize);
        }

        if(newPoolSize != oldPoolSize)
            log.info("Changing consumer pool size from " + oldPoolSize + " to " + newPoolSize + "");

        if(serverDataByAddress.isEmpty()) {
            log.info("No available servers with settings");
        } else {
            int knownServersCount = serverDataByAddress.size();

            log.info("Refreshed " + knownServersCount + " known server"
                    + (knownServersCount > 1 ? "s" : "") +" with settings:");

            for (ServerData serverData : serverDataByAddress.values()) {
                log.info(serverData.toString());
            }
        }
    }

    private MessageQueue<Message<?>> findOptimalMessageQueue() {
        return messageQueueByQueueId
                .values()
                .stream()
                .min(Comparator.comparingInt(MessageQueue::getCountListening))
                .orElseThrow(IllegalStateException::new);
    }
}