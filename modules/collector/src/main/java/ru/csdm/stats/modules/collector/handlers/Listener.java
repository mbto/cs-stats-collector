package ru.csdm.stats.modules.collector.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.DatagramsQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.ServerData;

import javax.annotation.PreDestroy;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;
import static ru.csdm.stats.common.utils.SomeUtils.addressToString;

@Service
@Lazy(false)
@Slf4j
public class Listener {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ThreadPoolTaskExecutor consumerTaskExecutor;

    @Autowired
    private Map<String, ServerData> availableAddresses;
    @Autowired
    private Map<String, Integer> registeredAddresses;
    @Autowired
    private Map<Integer, DatagramsQueue> datagramsInQueuesById;

    @Autowired
    private DatagramsConsumer datagramsConsumer;

    @Value("${stats.listener.port:8888}")
    private int listenerPort;

    private DatagramSocket datagramSocket;
    private int maxConsumers;
    private int nextQueueIdCounter;

    private boolean deactivated;

    @PreDestroy
    public void destroy() {
        if(log.isDebugEnabled())
            log.debug("destroy() start");

        deactivated = true;

        if(Objects.nonNull(datagramSocket)) {
            try {
                datagramSocket.close();
            } catch (Throwable ignored) { }
        }

        if(log.isDebugEnabled())
            log.debug("destroy() end");
    }

    @Async(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public void launchAsync() {
        maxConsumers = consumerTaskExecutor.getMaxPoolSize();

        log.info("Activating listener at port " + listenerPort + ", max consumers: " + maxConsumers);

        try {
            datagramSocket = new DatagramSocket(listenerPort);
        } catch (Throwable e) {
            log.warn("Failed initialize datagram socket at port " + listenerPort, e);

            int code = SpringApplication.exit(applicationContext, () -> 1);
            System.exit(code);
            return;
        }

        log.info("Listener started at " + addressToString(datagramSocket.getLocalSocketAddress()));

        while (true) {
            if (deactivated) {
                log.info("Deactivation detected");
                break;
            }

            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);

                onMessage(packet);
            } catch (Throwable e) {
                if (deactivated) {
                    log.info("Deactivation detected");
                    break;
                }

                log.warn("Exception while receiving datagram packet", e);
                continue;
            }
        }

        log.info("Deactivated");
    }

    public void onMessage(DatagramPacket packet) {
        String address = addressToString(packet.getSocketAddress());
        ServerData serverData = availableAddresses.get(address);

        if(Objects.isNull(serverData) || !serverData.isListening())
            return;

        byte[] data = packet.getData(); // [-1, -1, -1, -1, 108, 111, 103, 32, 76, ...]
        if(!(data.length >= 9 && (data[4] == 'l' && data[5] == 'o' && data[6] == 'g' && data[7] == ' ' && data[8] == 'L'))) {
            if(log.isDebugEnabled()) {
                log.debug(address + " Invalid data: '"
                        + new String(data, 0, packet.getLength(), StandardCharsets.UTF_8) + "'"
                        + ", raw: " + Arrays.toString(Arrays.copyOf(data, packet.getLength())));
            }

            return;
        }

        Integer queueId = registeredAddresses.get(address);
        if(Objects.isNull(queueId)) {
            queueId = nextQueueIdCounter;
            registeredAddresses.put(address, queueId);

            log.info(address + " registered with queue id: " + queueId);

            if(maxConsumers > 1) {
                if(++nextQueueIdCounter >= maxConsumers) {
                    nextQueueIdCounter = 0;
                }
            }
        }

        DatagramsQueue datagramsQueue = datagramsInQueuesById.get(queueId);
        if(Objects.isNull(datagramsQueue)) {
            datagramsQueue = new DatagramsQueue();
            datagramsInQueuesById.put(queueId, datagramsQueue);

            log.info("Created DatagramsQueue #" + datagramsInQueuesById.size());
            datagramsConsumer.startConsumeAsync(datagramsQueue);
        }

        Message message = new Message();
        message.setServerData(serverData);
        message.setPayload(new String(data, 8, packet.getLength() -8, StandardCharsets.UTF_8).trim());

        if(log.isDebugEnabled())
            log.debug(address + " Sending message: " + message);

        int tryes = 0;
        while (true) {
            try {
                ++tryes;
                datagramsQueue.getDatagramsQueue().putLast(message);
                break;
            } catch (InterruptedException e) {
                if(deactivated)
                    break;

                log.info(address + " InterruptedException catched, due put message " + message + " in datagramsQueue, " + tryes + "/3");

                if (tryes == 3) {
                    log.warn(address + " Failed put message " + message + " in datagramsQueue");
                    break;
                }
            }
        }
    }
}