package ru.csdm.stats.dao;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.UpdateSetFirstStep;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.model.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.tables.records.PlayerIpRecord;
import ru.csdm.stats.common.model.tables.records.PlayerRecord;
import ru.csdm.stats.common.model.tables.records.PlayerSteamidRecord;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.csdm.stats.common.model.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.tables.Player.PLAYER;
import static ru.csdm.stats.common.model.tables.PlayerIp.PLAYER_IP;
import static ru.csdm.stats.common.model.tables.PlayerSteamid.PLAYER_STEAMID;

@Repository
@Slf4j
public class CsStatsDao {
    @Autowired
    private DSLContext statsDsl;

    public List<KnownServer> fetchKnownServers() {
        return statsDsl.selectFrom(KNOWN_SERVER)
                .where(KNOWN_SERVER.ACTIVE.eq(true))
                .fetchInto(KnownServer.class);
    }

    public void mergePlayersStats(String address, List<PlayerRecord> playerRecords,
                                  Map<String, List<PlayerIpRecord>> ips,
                                  Map<String, List<PlayerSteamidRecord>> steamIds) {
        if(log.isDebugEnabled())
            log.debug("mergePlayersStats() start");

        statsDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            try {
                transactionalDsl.execute("LOCK TABLES " +
                        String.join(", ",
                                PLAYER.getName() + " WRITE",
                                PLAYER_IP.getName() + " WRITE",
                                PLAYER_STEAMID.getName() + " WRITE",
                                KNOWN_SERVER.getName() + " READ"
                        )
                );

                Map<UInteger, KnownServer> knownServerById = transactionalDsl
                        .select(KNOWN_SERVER.ID, KNOWN_SERVER.IPPORT)
                        .from(KNOWN_SERVER)
                        .fetchMap(KNOWN_SERVER.ID, KnownServer.class);

                Map<String, KnownServer> knownServerByAddress = knownServerById.values()
                        .stream()
                        .collect(Collectors.toMap(KnownServer::getIpport, Function.identity()));

                for (PlayerRecord playerRecord : playerRecords) {
                    UInteger playerId = transactionalDsl.select(PLAYER.ID)
                            .from(PLAYER)
                            // equals, not equalIgnoreCase, because `player`.`name` is CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
                            .where(PLAYER.NAME.eq(playerRecord.getName()))
                            .forUpdate()
                            .fetchOneInto(PLAYER.ID.getType());

                    // If at the time of saving - the known server ID changed or deleted,
                    // the data of which was saved in the cache, so that there are no errors
                    // on table constrains
                    if(!knownServerById.containsKey(playerRecord.getLastServerId())) {
                        // Attempt to search known server with a changed ID
                        KnownServer knownServer = knownServerByAddress.get(address);

                        if(Objects.nonNull(knownServer))
                            playerRecord.setLastServerId(knownServer.getId());
                        else
                            playerRecord.setLastServerId(null);
                    }

                    if (Objects.isNull(playerId)) {
                        playerId = transactionalDsl.insertInto(PLAYER)
                                .set(playerRecord)
                                .returning(PLAYER.ID)
                                .fetchOne().getId();
                    } else {
                        UpdateSetFirstStep<PlayerRecord> updateStep = transactionalDsl.update(PLAYER);

                        if (playerRecord.getKills().longValue() != 0) {
                            updateStep.set(PLAYER.KILLS, PLAYER.KILLS.plus(playerRecord.getKills()));
                        }

                        if (playerRecord.getDeaths().longValue() != 0) {
                            updateStep.set(PLAYER.DEATHS, PLAYER.DEATHS.plus(playerRecord.getDeaths()));
                        }

                        updateStep.set(PLAYER.TIME_SECS, PLAYER.TIME_SECS.plus(playerRecord.getTimeSecs()))
                                .set(PLAYER.LASTSEEN_DATETIME, playerRecord.getLastseenDatetime())
                                .set(PLAYER.LAST_SERVER_ID, playerRecord.getLastServerId())
                                .where(PLAYER.ID.eq(playerId))
                                .execute();
                    }

                    List<PlayerIpRecord> playerIpRecords = ips.get(playerRecord.getName());
                    if(!playerIpRecords.isEmpty()) {
                        List<String> existedIps = transactionalDsl.select(PLAYER_IP.IP)
                                .from(PLAYER_IP)
                                .where(PLAYER_IP.PLAYER_ID.eq(playerId),
                                        PLAYER_IP.IP.in(
                                                playerIpRecords.stream()
                                                        .map(PlayerIpRecord::getIp)
                                                        .collect(Collectors.toList())
                                        )).fetch(PLAYER_IP.IP);

                        playerIpRecords.removeIf(pir -> existedIps.contains(pir.getIp()));
                    }

                    for (PlayerIpRecord playerIpRecord : playerIpRecords) {
                        playerIpRecord.setPlayerId(playerId);

                        transactionalDsl.insertInto(PLAYER_IP)
                            .set(playerIpRecord)
                            .execute();
                    }

                    List<PlayerSteamidRecord> playerSteamIdRecords = steamIds.get(playerRecord.getName());
                    if(!playerSteamIdRecords.isEmpty()) {
                        List<String> existedSteamIds = transactionalDsl.select(PLAYER_STEAMID.STEAMID)
                                .from(PLAYER_STEAMID)
                                .where(PLAYER_STEAMID.PLAYER_ID.eq(playerId),
                                        PLAYER_STEAMID.STEAMID.in(
                                                playerSteamIdRecords.stream()
                                                        .map(PlayerSteamidRecord::getSteamid)
                                                        .collect(Collectors.toList())
                                        )).fetch(PLAYER_STEAMID.STEAMID);

                        playerSteamIdRecords.removeIf(psir -> existedSteamIds.contains(psir.getSteamid()));
                    }

                    for (PlayerSteamidRecord playerSteamIdRecord : playerSteamIdRecords) {
                        playerSteamIdRecord.setPlayerId(playerId);

                        transactionalDsl.insertInto(PLAYER_STEAMID)
                                .set(playerSteamIdRecord)
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