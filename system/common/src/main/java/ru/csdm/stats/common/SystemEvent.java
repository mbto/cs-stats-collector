package ru.csdm.stats.common;

public enum SystemEvent {
    CONSUME_DATAGRAM,
    REFRESH,
    FLUSH_FROM_FRONTEND,
    FLUSH_FROM_SCHEDULER,
    FLUSH_AND_QUIT,
}