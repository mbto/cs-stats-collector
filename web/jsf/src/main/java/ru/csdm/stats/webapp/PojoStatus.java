package ru.csdm.stats.webapp;

import java.util.EnumSet;

public enum PojoStatus {
    EXISTED,
    NEW,
    TO_REMOVE;

    public static final EnumSet<PojoStatus> ALLOWED_TO_FRONTEND = EnumSet.of(EXISTED, NEW);
}