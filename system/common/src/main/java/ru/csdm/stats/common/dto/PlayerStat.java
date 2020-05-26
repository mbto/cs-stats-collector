package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import ru.csdm.stats.common.utils.SomeUtils;

@Getter
@Setter
public class PlayerStat {
    private String name;
    private long totalKills;
    private long totalDeaths;
    private long totalTimeInSecs;

    @Override
    public String toString() {
        return name + ": totalKills=" + totalKills +
                ", totalDeaths=" + totalDeaths +
                ", totalTimeInSecs=" + totalTimeInSecs +
                " (" + SomeUtils.humanLifetime(totalTimeInSecs * 1000) + ")";
    }
}
