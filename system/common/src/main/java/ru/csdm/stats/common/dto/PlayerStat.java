package ru.csdm.stats.common.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.csdm.stats.common.utils.SomeUtils;

@Getter
@Setter
@EqualsAndHashCode
public class PlayerStat {
    private String name;
    private long totalKills;
    private long totalDeaths;
    private long totalTimeInSecs;

    @Override
    public String toString() {
        return name + ": kills=" + totalKills +
                ", deaths=" + totalDeaths +
                ", time=" + totalTimeInSecs + "s" +
                " (" + SomeUtils.humanLifetime(totalTimeInSecs * 1000) + ")";
    }
}
