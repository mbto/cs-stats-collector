package ru.csdm.stats;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.dto.view.PlayerSummary;

import static ru.csdm.stats.common.model.Routines.buildHumanTime;
import static ru.csdm.stats.common.model.Routines.buildStars;
import static ru.csdm.stats.common.model.Tables.RANK;
import static ru.csdm.stats.common.model.tables.Player.PLAYER;

@Repository
public class ViewsDao {
    @Autowired
    private DSLContext statsDsl;

    public PlayerSummary playerSummary(String name) {
        return statsDsl.select(PLAYER.ID,
                PLAYER.NAME,
                PLAYER.KILLS,
                PLAYER.DEATHS,
                buildHumanTime(PLAYER.TIME_SECS).as("gaming_time"),
                RANK.NAME.as("rank_name"),
                buildStars(RANK.LEVEL, statsDsl.selectCount().from(RANK).asField()).as("stars")
        ).from(PLAYER).leftJoin(RANK).on(PLAYER.RANK_ID.eq(RANK.ID))
                .where(PLAYER.NAME.eq(name))
                .fetchOneInto(PlayerSummary.class);
    }
}