package ru.csdm.stats.common;

public enum FlushEvent {
    FRONTEND("frontend"),
    SCHEDULER("scheduler"),
    PRE_DESTROY_LIFECYCLE("PreDestroy lifecycle"),
    NEW_GAME_MAP("started new game map"),
    SHUTDOWN_GAME_SERVER("shutdown game server")
    ;

    private final String eventName;

    FlushEvent(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String toString() {
        return "Event: '" + eventName + "'";
    }
}