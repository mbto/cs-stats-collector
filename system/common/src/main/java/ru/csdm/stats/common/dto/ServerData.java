package ru.csdm.stats.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.csdm.stats.common.model.tables.pojos.KnownServer;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ServerData {
    @JsonProperty("settings")
    @JsonIgnoreProperties({/*"id", */"ipport"})
    private KnownServer knownServer;
    private boolean listening;
    private LocalDateTime lastTouchDateTime;
}