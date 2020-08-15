package ru.csdm.stats;

import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import ru.csdm.stats.common.model.tables.pojos.Player;
import ru.csdm.stats.common.model.tables.pojos.PlayerIp;
import ru.csdm.stats.common.model.tables.pojos.PlayerSteamid;
import ru.csdm.stats.common.utils.SomeUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.csdm.stats.common.Constants.YYYYMMDD_HHMMSS_PATTERN;
import static ru.csdm.stats.common.model.tables.Player.PLAYER;
import static ru.csdm.stats.common.model.tables.PlayerIp.PLAYER_IP;
import static ru.csdm.stats.common.model.tables.PlayerSteamid.PLAYER_STEAMID;

@Getter
public class ActualDB {
    private List<Player> players;
    private List<PlayerIp> playersIps;
    private List<PlayerSteamid> playerSteamIds;

    public ActualDB(DSLContext dslContext) {
        this(dslContext, null);
    }

    public ActualDB(DSLContext dslContext, Map<Table<?>, List<Field<?>>> excludeColumns) {
        dslContext.transaction(configuration -> {
            DSLContext transactionalDsl = DSL.using(configuration);

            players = transactionalDsl.select(buildRequestedFields(PLAYER, excludeColumns))
                    .from(PLAYER)
                    .orderBy(PLAYER.TIME_SECS.desc(),
                            PLAYER.KILLS.desc(),
                            PLAYER.DEATHS.desc(),
                            PLAYER.NAME.asc()
                    ).fetchInto(Player.class);

            playersIps = transactionalDsl.select(buildRequestedFields(PLAYER_IP, excludeColumns))
                    .from(PLAYER_IP)
                    .orderBy(PLAYER_IP.ID.asc())
                    .fetchInto(PlayerIp.class);

            playerSteamIds = transactionalDsl.select(buildRequestedFields(PLAYER_STEAMID, excludeColumns))
                    .from(PLAYER_STEAMID)
                    .orderBy(PLAYER_STEAMID.ID.asc())
                    .fetchInto(PlayerSteamid.class);
        });

        System.out.println("\nActual players:");
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            System.out.println("{"
                    + String.join(", ",
                        quote(player.getId()),
                        quote(player.getName()),
                        quote(player.getKills()),
                        quote(player.getDeaths()),
                        quote(player.getTimeSecs()),
                        quote(player.getRankId()),
                        quote(player.getLastseenDatetime(), YYYYMMDD_HHMMSS_PATTERN),
                        quote(player.getLastServerId())
                    ) + "}" + (i + 1 < players.size() ? "," : "")
                    + " // " + SomeUtils.humanLifetime(player.getTimeSecs().longValue() * 1000)
            );
        }

        System.out.println("\nActual players ips:");
        for (int i = 0; i < playersIps.size(); i++) {
            PlayerIp playerIp = playersIps.get(i);

            System.out.println("{"
                    + String.join(", ", quote(playerIp.getId()),
                        quote(playerIp.getPlayerId()),
                        quote(playerIp.getIp()),
                        quote(playerIp.getRegDatetime(), YYYYMMDD_HHMMSS_PATTERN)
                    ) + "}" + (i + 1 < playersIps.size() ? "," : "")
            );
        }

        System.out.println("\nActual players steamIds:");
        for (int i = 0; i < playerSteamIds.size(); i++) {
            PlayerSteamid playerIp = playerSteamIds.get(i);

            System.out.println("{"
                    + String.join(", ", quote(playerIp.getId()),
                        quote(playerIp.getPlayerId()),
                        quote(playerIp.getSteamid()),
                        quote(playerIp.getRegDatetime(), YYYYMMDD_HHMMSS_PATTERN)
                    ) + "}" + (i + 1 < playerSteamIds.size() ? "," : "")
            );
        }
        System.out.println("");
    }
    
    private String quote(Object value) {
        if(Objects.isNull(value))
            return "null";
        
        return "\"" + value + "\"";
    }

    private String quote(LocalDateTime value, DateTimeFormatter pattern) {
        if(Objects.isNull(value))
            return "null";

        return "\"" + value.format(pattern) + "\"";
    }
    
    private List<Field<?>> buildRequestedFields(Table<?> table, Map<Table<?>, List<Field<?>>> excludeColumns) {
        if(Objects.isNull(excludeColumns) || excludeColumns.isEmpty())
            return Arrays.asList(table.fields());

        List<Field<?>> excludedFields = excludeColumns.get(table);
        if(Objects.isNull(excludedFields) || excludedFields.isEmpty())
            return Arrays.asList(table.fields());

        return Arrays.stream(table.fields())
                .filter(field -> !excludedFields.contains(field))
                .collect(Collectors.toList());
    }
}
