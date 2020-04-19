package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private ServerSetting serverSetting;
    private String payload;

    @Override
    public String toString() {
        return (serverSetting != null ? serverSetting.getIpport() : "-") + ": " + payload;
    }
}