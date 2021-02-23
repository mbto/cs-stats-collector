package ru.csdm.stats.common;

public enum BrokerEvent {
    CONSUME_DATAGRAM,
    REFRESH,
    FLUSH_FROM_FRONTEND,
    FLUSH_FROM_SCHEDULER,
    BREAK,
    FLUSH_ALL_AND_BREAK,
}