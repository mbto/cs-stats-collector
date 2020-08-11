package ru.csdm.stats.dao;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectLimitPercentStep;
import org.jooq.UpdateSetFirstStep;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.model.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.tables.pojos.Player;
import ru.csdm.stats.common.model.tables.records.PlayerIpRecord;
import ru.csdm.stats.common.model.tables.records.PlayerRecord;
import ru.csdm.stats.common.model.tables.records.PlayerSteamidRecord;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    public void mergePlayersStats(String address, List<PlayerRecord> plannedPlayers,
                                  Map<String, List<PlayerIpRecord>> plannedIpsByName,
                                  Map<String, List<PlayerSteamidRecord>> plannedSteamIdsByName) {
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

                for (PlayerRecord plannedPlayer : plannedPlayers) {
                    // If at the time of saving - the known server ID changed or deleted,
                    // the data of which was saved in the cache, so that there are no errors
                    // on table constrains
                    if(!knownServerById.containsKey(plannedPlayer.getLastServerId())) {
                        // Attempt to search known server with a changed ID
                        KnownServer knownServer = knownServerByAddress.get(address);

                        if(Objects.nonNull(knownServer))
                            plannedPlayer.setLastServerId(knownServer.getId());
                        else
                            plannedPlayer.setLastServerId(null);
                    }

                    Player player = transactionalDsl.select(PLAYER.ID, PLAYER.NAME)
                            .from(PLAYER)
                            // equals, not equalIgnoreCase, because `player`.`name` is CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
                            .where(PLAYER.NAME.eq(plannedPlayer.getName()))
                            .forUpdate()
                            .fetchOneInto(Player.class);

                    UInteger playerId;
                    if (Objects.isNull(player)) {
                        playerId = transactionalDsl.insertInto(PLAYER)
                                .set(plannedPlayer)
                                .returning(PLAYER.ID)
                                .fetchOne().getId();
                    } else {
                        playerId = player.getId();

                        UpdateSetFirstStep<PlayerRecord> updateStep = transactionalDsl.update(PLAYER);

                        if (!player.getName().equals(plannedPlayer.getName())) {
                            updateStep.set(PLAYER.NAME, plannedPlayer.getName());
                        }

                        if (plannedPlayer.getKills().longValue() != 0) {
                            updateStep.set(PLAYER.KILLS, PLAYER.KILLS.plus(plannedPlayer.getKills()));
                        }

                        if (plannedPlayer.getDeaths().longValue() != 0) {
                            updateStep.set(PLAYER.DEATHS, PLAYER.DEATHS.plus(plannedPlayer.getDeaths()));
                        }

                        updateStep.set(PLAYER.TIME_SECS, PLAYER.TIME_SECS.plus(plannedPlayer.getTimeSecs()))
                                .set(PLAYER.LASTSEEN_DATETIME, plannedPlayer.getLastseenDatetime())
                                .set(PLAYER.LAST_SERVER_ID, plannedPlayer.getLastServerId())
                                .where(PLAYER.ID.eq(playerId))
                                .execute();
                    }

                    List<PlayerIpRecord> plannedIps = plannedIpsByName.get(plannedPlayer.getName());
                    if(!plannedIps.isEmpty()) {
                        Set<String> existedIps = transactionalDsl.select(PLAYER_IP.IP)
                                .from(PLAYER_IP)
                                .where(PLAYER_IP.PLAYER_ID.eq(playerId),
                                        PLAYER_IP.IP.in(
                                                plannedIps.stream()
                                                        .map(PlayerIpRecord::getIp)
                                                        .collect(Collectors.toSet())
                                        )).fetchSet(PLAYER_IP.IP);

                        plannedIps.removeIf(pir -> existedIps.contains(pir.getIp()));
                    }

                    for (PlayerIpRecord plannedIp : plannedIps) {
                        plannedIp.setPlayerId(playerId);
                    }
                    transactionalDsl.batchInsert(plannedIps).execute();

                    int ipsCount = transactionalDsl.selectCount()
                            .from(PLAYER_IP)
                            .where(PLAYER_IP.PLAYER_ID.eq(playerId))
                            .fetchOneInto(int.class);

                    if(ipsCount > 15) {
                        SelectLimitPercentStep<Record1<UInteger>> subquery = DSL.select(PLAYER_IP.ID)
                                .from(PLAYER_IP)
                                .where(PLAYER_IP.PLAYER_ID.eq(playerId))
                                .orderBy(PLAYER_IP.REG_DATETIME.asc())
                                .limit(ipsCount - 15);

                        transactionalDsl.deleteFrom(PLAYER_IP)
                                .where(PLAYER_IP.PLAYER_ID.eq(playerId),
                                       PLAYER_IP.ID.in(DSL.select(subquery.field(PLAYER_IP.ID)).from(subquery))
                                ).execute();
                    }

                    List<PlayerSteamidRecord> plannedSteamIds = plannedSteamIdsByName.get(plannedPlayer.getName());
                    if(!plannedSteamIds.isEmpty()) {
                        Set<String> existedSteamIds = transactionalDsl.select(PLAYER_STEAMID.STEAMID)
                                .from(PLAYER_STEAMID)
                                .where(PLAYER_STEAMID.PLAYER_ID.eq(playerId),
                                        PLAYER_STEAMID.STEAMID.in(
                                                plannedSteamIds.stream()
                                                        .map(PlayerSteamidRecord::getSteamid)
                                                        .collect(Collectors.toSet())
                                        )).fetchSet(PLAYER_STEAMID.STEAMID);

                        plannedSteamIds.removeIf(psir -> existedSteamIds.contains(psir.getSteamid()));
                    }

                    for (PlayerSteamidRecord plannedSteamId : plannedSteamIds) {
                        plannedSteamId.setPlayerId(playerId);
                    }
                    transactionalDsl.batchInsert(plannedSteamIds).execute();

                    int steamIdsCount = transactionalDsl.selectCount()
                            .from(PLAYER_STEAMID)
                            .where(PLAYER_STEAMID.PLAYER_ID.eq(playerId))
                            .fetchOneInto(int.class);

                    if(steamIdsCount > 15) {
                        SelectLimitPercentStep<Record1<UInteger>> subquery = DSL.select(PLAYER_STEAMID.ID)
                                .from(PLAYER_STEAMID)
                                .where(PLAYER_STEAMID.PLAYER_ID.eq(playerId))
                                .orderBy(PLAYER_STEAMID.REG_DATETIME.asc())
                                .limit(steamIdsCount - 15);

                        transactionalDsl.deleteFrom(PLAYER_STEAMID)
                                .where(PLAYER_STEAMID.PLAYER_ID.eq(playerId),
                                        PLAYER_STEAMID.ID.in(DSL.select(subquery.field(PLAYER_STEAMID.ID)).from(subquery))
                                ).execute();
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