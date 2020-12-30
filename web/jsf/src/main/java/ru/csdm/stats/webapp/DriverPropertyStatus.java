package ru.csdm.stats.webapp;

import java.util.EnumSet;

public enum DriverPropertyStatus {
    EXISTED,
    NEW,
    TO_REMOVE;

    public static final EnumSet<DriverPropertyStatus> ALLOWED_TO_FRONTEND = EnumSet.of(EXISTED, NEW);
}