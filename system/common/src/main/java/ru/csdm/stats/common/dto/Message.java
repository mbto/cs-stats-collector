package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private ServerData serverData;
    private String payload;

    @Override
    public String toString() {
        return serverData.getServerSetting().getIpport() + ": " + payload;
    }
}