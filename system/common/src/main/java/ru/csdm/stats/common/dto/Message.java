package ru.csdm.stats.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Message {
    private String address;
    private String payload;

    @Override
    public String toString() {
        return address + ": " + payload;
    }
}