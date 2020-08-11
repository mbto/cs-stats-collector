package ru.csdm.stats.modules.collector.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.Session;
import ru.csdm.stats.common.model.tables.records.PlayerIpRecord;
import ru.csdm.stats.common.model.tables.records.PlayerRecord;
import ru.csdm.stats.common.model.tables.records.PlayerSteamidRecord;
import ru.csdm.stats.dao.CsStatsDao;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.csdm.stats.common.utils.SomeUtils.playerRecordToString;

@Service
@Lazy(false)
@Slf4j
public class PlayersSender {
    @Autowired
    private CsStatsDao csStatsDao;

    @Async("playersSenderTaskExecutor")
    public void sendAsync(String address, List<CollectedPlayer> collectedPlayers) {
        log.info(address + " Calculating stats from " + collectedPlayers.size() + " player" + (collectedPlayers.size() > 1 ? "s" : ""));

        Map<String, List<PlayerIpRecord>> plannedIpsByName = new HashMap<>();
        Map<String, List<PlayerSteamidRecord>> plannedSteamIdsByName = new HashMap<>();

        List<PlayerRecord> plannedPlayers = collectedPlayers
                .stream()
                .map(collectedPlayer -> {
                    long totalKills = 0;
                    long totalDeaths = 0;
                    long totalTimeInSecs = 0;

                    for (Session session : collectedPlayer.getSessions()) {
                        totalKills += session.getKills();
                        totalDeaths += session.getDeaths();

                        long diff = Duration.between(session.getStarted(), session.getFinished()).getSeconds();

                        if(diff > 0)
                            totalTimeInSecs += diff;
                    }

                    if(totalKills == 0 && totalDeaths == 0 && totalTimeInSecs == 0)
                        return null;

                    PlayerRecord plannedPlayer = new PlayerRecord();
                    plannedPlayer.setName(collectedPlayer.getName());
                    plannedPlayer.setKills(UInteger.valueOf(totalKills));
                    plannedPlayer.setDeaths(UInteger.valueOf(totalDeaths));
                    plannedPlayer.setTimeSecs(UInteger.valueOf(totalTimeInSecs));
                    plannedPlayer.setLastServerId(collectedPlayer.getLastServerId());
                    plannedPlayer.setLastseenDatetime(collectedPlayer.getLastseenDatetime());

                    plannedIpsByName.put(collectedPlayer.getName(), collectedPlayer.getIpAddresses().stream()
                            .map(ip -> {
                                PlayerIpRecord playerIpRecord = new PlayerIpRecord();
                                playerIpRecord.setIp(ip);
                                playerIpRecord.setRegDatetime(collectedPlayer.getLastseenDatetime());
                                return playerIpRecord;
                            }).collect(Collectors.toList()));

                    plannedSteamIdsByName.put(collectedPlayer.getName(), collectedPlayer.getSteamIds().stream()
                            .map(steamId -> {
                                PlayerSteamidRecord playerSteamIdRecord = new PlayerSteamidRecord();
                                playerSteamIdRecord.setSteamid(steamId);
                                playerSteamIdRecord.setRegDatetime(collectedPlayer.getLastseenDatetime());
                                return playerSteamIdRecord;
                            }).collect(Collectors.toList()));

                    return plannedPlayer;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if(plannedPlayers.isEmpty()) {
            log.info(address + " Skip flushing players stats, due empty plannedPlayers");
            return;
        }

        log.info(address + " Flushing " + plannedPlayers.size() + " player" + (plannedPlayers.size() > 1 ? "s" : "") + " stats");

        for (PlayerRecord plannedPlayer : plannedPlayers) {
            log.info(address + " " + playerRecordToString(plannedPlayer));
        }

        try {
            csStatsDao.mergePlayersStats(address, plannedPlayers, plannedIpsByName, plannedSteamIdsByName);

            log.info(address + " Successfully merged " + plannedPlayers.size() +
                    " player" + (plannedPlayers.size() > 1 ? "s" : "") + " stats");
        } catch (Throwable e) {
            log.warn(address + " Failed merging " + plannedPlayers.size() +
                    " player" + (plannedPlayers.size() > 1 ? "s" : "") + " stats", e);
        }
    }
}