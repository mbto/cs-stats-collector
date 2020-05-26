package ru.csdm.stats.dao;

import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.dto.PlayerStat;
import ru.csdm.stats.common.dto.ServerSetting;

import java.util.List;
import java.util.Objects;

@Repository
@Slf4j
public class CsStatsDao {
    @Autowired
    private DSLContext statsDsl;

    private static final Table<Record> csstats_servers_table = DSL.table("csstats_servers");
//    private static final Field<String> ipport_field = DSL.field("ipport", String.class);
    private static final Field<Boolean> active_field = DSL.field("active", Boolean.class);
//    private static final Field<Boolean> ffa_field = DSL.field("ffa", Boolean.class);
//    private static final Field<Boolean> ignore_bots_field = DSL.field("ignore_bots", Boolean.class);

    public List<ServerSetting> fetchServersSettings() {
        return statsDsl.select(DSL.asterisk())
                .from(csstats_servers_table)
                .where(active_field.eq(true))
                .fetchInto(ServerSetting.class);
    }

    private static final Table<Record> csstats_table = DSL.table("csstats");
    private static final Field<Long> id_field = DSL.field("id", Long.class);
    private static final Field<String> name_field = DSL.field("name", String.class);
    private static final Field<Long> kills_field = DSL.field("kills", Long.class);
    private static final Field<Long> deaths_field = DSL.field("deaths", Long.class);
    private static final Field<Long> time_secs_field = DSL.field("time_secs", Long.class);

    public void mergePlayersStats(List<PlayerStat> playerStats) {
        if(log.isDebugEnabled())
            log.debug("mergePlayersStats() start");

        statsDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            try {
                transactionalDsl.execute("LOCK TABLES " + csstats_table.getName() + " WRITE");

                for (PlayerStat stat : playerStats) {
                    Long id = transactionalDsl.select(id_field)
                            .from(csstats_table)
                            .where(name_field.equalIgnoreCase(stat.getName()))
                            .forUpdate()
                            .fetchOneInto(id_field.getType());

                    if (Objects.isNull(id)) {
                        transactionalDsl.insertInto(csstats_table)
                                .columns(name_field,
                                        kills_field,
                                        deaths_field,
                                        time_secs_field
                                ).values(stat.getName(),
                                stat.getTotalKills(),
                                stat.getTotalDeaths(),
                                stat.getTotalTimeInSecs()
                        ).execute();
                    } else {
                        UpdateSetStep<Record> updateStep = transactionalDsl.update(csstats_table);

                        if (stat.getTotalKills() != 0) {
                            updateStep.set(kills_field, kills_field.plus(stat.getTotalKills()));
                        }

                        if (stat.getTotalDeaths() != 0) {
                            updateStep.set(deaths_field, deaths_field.plus(stat.getTotalDeaths()));
                        }

                        updateStep.set(time_secs_field, time_secs_field.plus(stat.getTotalTimeInSecs()))
                                .where(id_field.eq(id))
                                .execute();
                    }
                }
            } finally {
                transactionalDsl.execute("UNLOCK TABLES");
            }
        });

        if(log.isDebugEnabled())
            log.debug("mergePlayersStats() end");
    }
}