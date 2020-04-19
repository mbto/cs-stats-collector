package ru.csdm.stats.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ServerSetting {
    @JsonIgnore
    private String ipport;
    private Boolean ffa;
}
