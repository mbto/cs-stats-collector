package ru.csdm.stats.webapp;

import lombok.Getter;
import lombok.Setter;

import javax.faces.model.SelectItem;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class AggregatedPlayer {
    private String name;
    private long kills;
    private long deaths;
    private LocalDateTime started;
    private LocalDateTime finished;
    private Set<SelectItem> ipAddresses;
    private Set<SelectItem> steamIds;
}
