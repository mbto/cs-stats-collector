package ru.csdm.stats.common.dto;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

@Getter
public class MessageQueue {
    private final int queueId;
    private final Set<String> knownServersPorts = new HashSet<>();
    private final LinkedBlockingDeque<Message<?>> messageQueue;

    public MessageQueue(int queueId) {
        this.queueId = queueId;

        /* Integer.MAX_VALUE - Maximum number of Message objects,
           other objects will be rejected by ThreadPoolExecutor.DiscardPolicy() */
        this.messageQueue = new LinkedBlockingDeque<>(Integer.MAX_VALUE);
    }

    public void addPort(String port) {
        knownServersPorts.add(port);
    }
    public void removePort(String port) {
        knownServersPorts.remove(port);
    }

    @Override
    public String toString() {
        return "MessageQueue [" + queueId + "]";
    }
}