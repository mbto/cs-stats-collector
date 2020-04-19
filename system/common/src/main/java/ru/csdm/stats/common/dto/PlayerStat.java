package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlayerStat {
    private String name;
    private long totalKills;
    private long totalDeaths;
    private long totalTimeInSecs;
}
