package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ServerData {
    private ServerSetting serverSetting;
    public boolean listening;
    private LocalDateTime lastTouchDateTime;
}