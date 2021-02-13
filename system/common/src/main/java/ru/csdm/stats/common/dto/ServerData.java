package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static ru.csdm.stats.common.Constants.SERVER_DATA_MESSAGES_MAX;
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
    private List<Pair<LocalDateTime, String>> messages = new CopyOnWriteArrayList<>();

    public void addMessage(String message) {
        messages.add(Pair.of(LocalDateTime.now(), message));
        reduceMessagesCount();
    }

    public void addMessages(Collection<String> messages) {
        LocalDateTime now = LocalDateTime.now();
        this.messages.addAll(messages.stream()
                .map(str -> Pair.of(now, str))
                .collect(Collectors.toList()));

        reduceMessagesCount();
    }

    private void reduceMessagesCount() {
        int size = messages.size();
        if(size > SERVER_DATA_MESSAGES_MAX) {
            messages = messages.subList(size - SERVER_DATA_MESSAGES_MAX, size);
        }
    }

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