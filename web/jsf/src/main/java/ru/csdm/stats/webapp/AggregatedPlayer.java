package ru.csdm.stats.webapp;

import lombok.Getter;
import lombok.Setter;
import ru.csdm.stats.common.utils.SomeUtils;

import java.time.LocalDateTime;
import java.util.Objects;

import static ru.csdm.stats.common.Constants.YYYYMMDD_HHMMSS_PATTERN;

@Getter
@Setter
public class AggregatedPlayer {
    private String name;
    private long kills;
    private long deaths;
    private LocalDateTime started;
    private LocalDateTime finished;
    private String ipAddresses;
    private String steamIds;

    public String getDates() {
        return YYYYMMDD_HHMMSS_PATTERN.format(started)
                + (Objects.nonNull(finished) ? " - " + YYYYMMDD_HHMMSS_PATTERN.format(finished) : "");
    }

    public String getLifetime() {
        return SomeUtils.humanLifetime(started, finished);
    }
}