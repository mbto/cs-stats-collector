package ru.csdm.stats;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.common.model.csstats.tables.pojos.Player;
import ru.csdm.stats.common.model.csstats.tables.pojos.PlayerIp;
import ru.csdm.stats.common.model.csstats.tables.pojos.PlayerSteamid;
import ru.csdm.stats.common.utils.SomeUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.csdm.stats.common.Constants.YYYYMMDD_HHMMSS_PATTERN;
import static ru.csdm.stats.common.model.csstats.Tables.HISTORY;
import static ru.csdm.stats.common.model.csstats.tables.Player.PLAYER;
import static ru.csdm.stats.common.model.csstats.tables.PlayerIp.PLAYER_IP;
import static ru.csdm.stats.common.model.csstats.tables.PlayerSteamid.PLAYER_STEAMID;
import static ru.csdm.stats.common.utils.SomeUtils.buildHikariDataSource;
import static ru.csdm.stats.common.utils.SomeUtils.configJooqContext;

@Component
@Profile("test")
@Slf4j
public class ProjectMaker {
    @Getter
    private List<Player> players;
    @Getter
    private List<PlayerIp> playersIps;
    @Getter
    private List<PlayerSteamid> playerSteamIds;

    private List<TableField<?,?>> excludeColumns;

    public void process(Project project, Runnable job) {
        process(project, job, null);
    }

    public void process(Project project, Runnable job, List<TableField<?,?>> excludeColumns) {
        this.excludeColumns = excludeColumns;

        try(HikariDataSource hds = buildHikariDataSource("pool-" + project.getDatabaseSchema() + " [" + project.getId() + "] " + project.getName())) {
            hds.setJdbcUrl("jdbc:mysql://" + project.getDatabaseHostport() + "/" + project.getDatabaseSchema());
            hds.setSchema(project.getDatabaseSchema());
            hds.setUsername(project.getDatabaseUsername());
            hds.setPassword(project.getDatabasePassword());

            if (Objects.nonNull(project.getDatabaseServerTimezone()))
                hds.addDataSourceProperty("serverTimezone", project.getDatabaseServerTimezone().getLiteral());

            /* Override settings from com.zaxxer.hikari.HikariConfig */
            hds.setMaximumPoolSize(2);
            hds.setMinimumIdle(1);

            hds.setConnectionTimeout(SECONDS.toMillis(10));
            hds.setValidationTimeout(SECONDS.toMillis(5));
            hds.setIdleTimeout(SECONDS.toMillis(29));
            hds.setMaxLifetime(SECONDS.toMillis(30));

            log.info("Using datasource settings: jdbcUrl=" + hds.getJdbcUrl()
                    + ", schema=" + hds.getSchema()
                    + ", dataSourceProperties=" + hds.getDataSourceProperties());

            DSLContext statsDsl = configJooqContext(hds, SQLDialect.MYSQL, project.getDatabaseSchema(), 10);

            statsDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                try {
                    transactionalDsl.execute("SET FOREIGN_KEY_CHECKS = 0;");

                    transactionalDsl.truncate(PLAYER).execute();
                    transactionalDsl.truncate(PLAYER_IP).execute();
                    transactionalDsl.truncate(PLAYER_STEAMID).execute();
                    transactionalDsl.truncate(HISTORY).execute();
                } finally {
                    transactionalDsl.execute("SET FOREIGN_KEY_CHECKS = 1;");
                }
            });

            job.run();

            statsDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                players = transactionalDsl.select(buildRequestedFields(PLAYER))
                        .from(PLAYER)
                        .orderBy(PLAYER.TIME_SECS.desc(),
                                PLAYER.KILLS.desc(),
                                PLAYER.DEATHS.desc(),
                                PLAYER.NAME.asc()
                        ).fetchInto(Player.class);

                playersIps = transactionalDsl.select(buildRequestedFields(PLAYER_IP))
                        .from(PLAYER_IP)
                        .orderBy(PLAYER_IP.ID.asc())
                        .fetchInto(PlayerIp.class);

                playerSteamIds = transactionalDsl.select(buildRequestedFields(PLAYER_STEAMID))
                        .from(PLAYER_STEAMID)
                        .orderBy(PLAYER_STEAMID.ID.asc())
                        .fetchInto(PlayerSteamid.class);
            });
        }

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
                        quote(player.getLastServerName())
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
    
    private List<Field<?>> buildRequestedFields(Table<?> table) {
        if(Objects.isNull(excludeColumns) || excludeColumns.isEmpty())
            return Arrays.asList(table.fields());

        Map<Table<?>, List<Field<?>>> excludeColumnsByTable = excludeColumns
                .stream()
                .collect(Collectors.groupingBy(field -> ((TableField<?,?>) field).getTable()));

        List<Field<?>> excludedFields = excludeColumnsByTable.get(table);
        if(Objects.isNull(excludedFields) || excludedFields.isEmpty())
            return Arrays.asList(table.fields());

        return Arrays.stream(table.fields())
                .filter(field -> !excludedFields.contains(field))
                .collect(Collectors.toList());
    }
}