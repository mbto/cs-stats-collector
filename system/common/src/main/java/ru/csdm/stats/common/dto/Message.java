package ru.csdm.stats.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.csdm.stats.common.BrokerEvent;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message<T> {
    private String payload;
    private T pojo;
    private BrokerEvent brokerEvent;

    @Override
    public String toString() {
        return "Payload(" + payload + ")"
                + (Objects.nonNull(pojo) ? " Pojo(" + pojo.getClass().getSimpleName() + ")" : "")
                + (Objects.nonNull(brokerEvent) ? " BrokerEvent(" + brokerEvent + ")" : "");
    }
}