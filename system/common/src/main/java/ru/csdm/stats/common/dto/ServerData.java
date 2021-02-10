package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;

import java.time.LocalDateTime;
import java.util.List;

import static ru.csdm.stats.common.utils.SomeUtils.*;

@Getter
@Setter
public class ServerData {
    private KnownServer knownServer;
    private boolean listening;
    private LocalDateTime lastTouchDateTime;
    private LocalDateTime nextFlushDateTime;
    private Project project;
    private List<DriverProperty> driverProperties;

    @Override
    public String toString() {
        return String.format("%-15s ", listening ? "[LISTENING]" : "[NOT LISTENING]") + knownServer.getIpport()
                + ": FFA: " + humanBoolean(knownServer.getFfa())
                + ", Ignore bots: " + humanBoolean(knownServer.getIgnoreBots())
                + ", Start session on action: " + humanBoolean(knownServer.getStartSessionOnAction())
                + ", Known server: [" + knownServer.getId() + "] " + knownServer.getName()
                + ", Project: [" + project.getId() + "] " + project.getName();
    }
}