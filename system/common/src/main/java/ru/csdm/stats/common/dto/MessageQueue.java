package ru.csdm.stats.common.dto;

import lombok.Getter;

import java.util.concurrent.LinkedBlockingDeque;

@Getter
public class MessageQueue<T> {
    private final int queueId;
    private int countListening;
    private final LinkedBlockingDeque<T> messageQueue;

    public MessageQueue(int queueId) {
        this.queueId = queueId;

        /* Integer.MAX_VALUE - Maximum number of Message objects,
           other objects will be rejected by ThreadPoolExecutor.DiscardPolicy() */
        this.messageQueue = new LinkedBlockingDeque<>(Integer.MAX_VALUE);
    }

    public void incListening() {
        ++countListening;
    }
    public void decListening() {
        --countListening;
    }

    @Override
    public String toString() {
        return "MessageQueue [" + queueId + "]";
    }
}