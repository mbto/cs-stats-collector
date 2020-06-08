package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import ru.csdm.stats.common.SystemEvent;

import java.util.Objects;

@Getter
@Setter
public class Message {
    private ServerData serverData;
    private String payload;
    private SystemEvent systemEvent;

    @Override
    public String toString() {
        return payload
                + (Objects.nonNull(systemEvent) ? " (" + systemEvent + ")": "");
    }
}