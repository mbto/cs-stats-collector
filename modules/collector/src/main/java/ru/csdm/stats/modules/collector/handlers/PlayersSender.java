package ru.csdm.stats.modules.collector.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.Player;
import ru.csdm.stats.common.dto.PlayerStat;
import ru.csdm.stats.common.dto.Session;
import ru.csdm.stats.dao.CsStatsDao;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Lazy(false)
@Slf4j
public class PlayersSender {
    @Autowired
    private CsStatsDao csStatsDao;

    @Async("playersSenderTaskExecutor")
    public void sendAsync(String address, List<Player> players) {
        log.info(address + " Preparing " + players.size() + " player" + (players.size() > 1 ? "s" : ""));

        List<PlayerStat> playerStats = players
                .stream()
                .map(player -> {
                    String name = player.getName();

                    long totalKills = 0;
                    long totalDeaths = 0;
                    long totalTimeInSecs = 0;

                    for (Session session : player.getSessions()) {
                        totalKills += session.getKills();
                        totalDeaths += session.getDeaths();

                        long diff = Duration.between(session.getStarted(), session.getFinished()).getSeconds();

                        if(diff > 0)
                            totalTimeInSecs += diff;
                    }

                    if(totalKills == 0 && totalDeaths == 0 && totalTimeInSecs == 0)
                        return null;

                    PlayerStat stat = new PlayerStat();
                    stat.setName(name);
                    stat.setTotalKills(totalKills);
                    stat.setTotalDeaths(totalDeaths);
                    stat.setTotalTimeInSecs(totalTimeInSecs);

                    return stat;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if(playerStats.isEmpty()) {
            log.info(address + " Skip flushing players stats, due empty playerStats");
            return;
        }

        log.info(address + " Flushing " + playerStats.size() + " player" + (playerStats.size() > 1 ? "s" : "") + " stats");

        for (PlayerStat stat : playerStats) {
            log.info(address + " " + stat);
        }

        try {
            csStatsDao.mergePlayersStats(playerStats);

            log.info(address + " Successed merged " + playerStats.size() +
                    " player" + (playerStats.size() > 1 ? "s" : "") + " stats");
        } catch (Throwable e) {
            log.warn(address + " Failed merging " + playerStats.size() +
                    " player" + (playerStats.size() > 1 ? "s" : "") + " stats", e);
        }
    }
}