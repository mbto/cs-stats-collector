package ru.csdm.stats.modules.collector.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.dto.Player;
import ru.csdm.stats.common.dto.PlayerStat;
import ru.csdm.stats.common.dto.Session;
import ru.csdm.stats.dao.AmxDao;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Lazy(false)
@Slf4j
public class PlayersSender {
    @Autowired
    private AmxDao amxDao;

    @Async("playersSenderTaskExecutor")
    public void sendAsync(String address, List<Player> players) {
        log.info(address + " Flushing " + players.size() + " player" + (players.size() > 1 ? "s" : ""));

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

                        totalTimeInSecs += Duration.between(session.getStarted(), session.getFinished()).getSeconds();
                    }

                    PlayerStat stat = new PlayerStat();
                    stat.setName(name);
                    stat.setTotalKills(totalKills);
                    stat.setTotalDeaths(totalDeaths);
                    stat.setTotalTimeInSecs(totalTimeInSecs);

                    return stat;
                }).collect(Collectors.toList());

        if(playerStats.isEmpty())
            return;

        if(log.isDebugEnabled()) {
            for (PlayerStat stat : playerStats) {
                log.debug(address + " " + stat);
            }
        }

        try {
            amxDao.mergePlayersStats(playerStats);
        } catch (Throwable e) {
            log.warn(address + " Batch " + playerStats.size() + " playerStats failed", e);
        }
    }
}