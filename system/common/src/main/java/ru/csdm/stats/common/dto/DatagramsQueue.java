package ru.csdm.stats.common.dto;

import lombok.Getter;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Wrapper for LinkedBlockingDeque
 * Using wrapper, because maybe add a new fields to this class
 */
public class DatagramsQueue {
    @Getter
    private final LinkedBlockingDeque<Message> datagramsQueue = new LinkedBlockingDeque<>();
}