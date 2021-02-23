package ru.csdm.stats.common.dto;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

@Getter
public class MessageQueue {
    private final int queueId;
    private final LinkedBlockingDeque<Message<?>> messageQueue;
    private final Map<Boolean, Set<String>> knownServersPorts;

    public MessageQueue(int queueId) {
        this.queueId = queueId;

        /* Integer.MAX_VALUE - Maximum number of Message objects,
           other objects will be rejected by ThreadPoolExecutor.DiscardPolicy() */
        this.messageQueue = new LinkedBlockingDeque<>(Integer.MAX_VALUE);

        this.knownServersPorts = new HashMap<>(2, 1f);
        this.knownServersPorts.put(true, new HashSet<>());
        this.knownServersPorts.put(false, new HashSet<>());
    }

    public void addPort(String port, boolean isActive) {
        knownServersPorts.get(isActive).add(port);
    }

    public void removePort(String port, boolean isActive) {
        knownServersPorts.get(isActive).remove(port);
    }

    public int countPorts(boolean isActive) {
        return knownServersPorts.get(isActive).size();
    }

    public boolean zeroPorts() {
        return knownServersPorts.get(true).size() == 0
                && knownServersPorts.get(false).size() == 0;
    }

    public void clearPorts() {
        knownServersPorts.get(true).clear();
        knownServersPorts.get(false).clear();
    }

    @Override
    public String toString() {
        return "MessageQueue [" + queueId + "]";
    }
}