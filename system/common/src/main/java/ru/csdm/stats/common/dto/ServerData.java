package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.csdm.stats.common.model.tables.pojos.KnownServer;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ServerData {
    private KnownServer knownServer;
    private boolean listening;
    private LocalDateTime lastTouchDateTime;
}