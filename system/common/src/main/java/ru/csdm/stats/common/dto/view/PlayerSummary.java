package ru.csdm.stats.common.dto.view;

import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;

@Getter
@Setter
public class PlayerSummary {
    private UInteger id;
    private String name;
    private UInteger kills;
    private UInteger deaths;
    private String human_time;
    private String rank_name;
    private String stars;
}