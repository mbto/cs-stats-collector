package ru.csdm.stats.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.csdm.stats.common.SystemEvent;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message<T> {
    private String payload;
    private T pojo;
    private SystemEvent systemEvent;

    @Override
    public String toString() {
        return "Payload(" + payload + ")"
                + (Objects.nonNull(pojo) ? " pojo exists" : "")
                + (Objects.nonNull(systemEvent) ? " SystemEvent(" + systemEvent + ")" : "");
    }
}