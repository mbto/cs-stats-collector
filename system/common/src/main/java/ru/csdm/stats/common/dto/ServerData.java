package ru.csdm.stats.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class ServerData {
    @JsonProperty("settings")
    @JsonIgnoreProperties({"ipport", "projectId", "instanceId"})
    private KnownServer knownServer;

    private boolean listening;

    private LocalDateTime lastTouchDateTime;

    private LocalDateTime nextFlushDateTime;

    @JsonIgnoreProperties({"description", "regDatetime", "databaseHostport",
            "databaseHostport", "databaseSchema", "databaseUsername",
            "databasePassword", "databaseServerTimezone"})
    private Project project;

    @JsonIgnore
    private List<DriverProperty> driverProperties;
}