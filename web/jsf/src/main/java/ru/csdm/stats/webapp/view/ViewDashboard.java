package ru.csdm.stats.webapp.view;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedCaseInsensitiveMap;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.service.CollectorService;
import ru.csdm.stats.service.SettingsService;
import ru.csdm.stats.webapp.DependentUtil;
import ru.csdm.stats.webapp.application.ChangesCounter;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static ru.csdm.stats.common.SystemEvent.FLUSH_FROM_ENDPOINT;

@ViewScoped
@Named
@Slf4j
public class ViewDashboard {
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private CollectorService collectorService;
    @Autowired
    private ChangesCounter changesCounter;
    @Autowired
    private DependentUtil util;

    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    public void updateSettings() {
        log.info("Manager requested updateSettings()");

        settingsService.updateSettings(false);
        changesCounter.getCounter().set(0);
    }

    public Map<String, String> flush() {
        log.info("Manager requested flush()");

        Map<String, String> flush = collectorService.flush(FLUSH_FROM_ENDPOINT, false);

        changesCounter.getCounter().set(0);

        return flush;
    }

    public void onRowSelect(SelectEvent event) {
        String address = ((Map.Entry<String, CollectedPlayer>) event.getObject()).getKey();

        util.sendRedirect("/sessions?address=" + address);
    }

    public void makeFakes() {
        for (int port = 27014; port <= 27020; port++) {
            Map<String, CollectedPlayer> gameSessions = new LinkedCaseInsensitiveMap<>();

            int playersCount = ThreadLocalRandom.current().nextInt(0, 60);
            for (int i = 0; i < playersCount; i++) {
                int nameLen = ThreadLocalRandom.current().nextInt(1, 32);

                String randName = RandomStringUtils
                        .randomAlphanumeric(nameLen)
                        .replaceAll(" {2}", " ")
                        .replaceAll("[a-m]", " ").trim();
                if (StringUtils.isBlank(randName))
                    randName = RandomStringUtils.randomAlphanumeric(1);

                CollectedPlayer collectedPlayer = new CollectedPlayer(randName);

                int sessions = ThreadLocalRandom.current().nextInt(1, 5);
                for (int k = 0; k < sessions; k++) {
                    int kills = ThreadLocalRandom.current().nextInt(0, 10);

                    ChronoUnit chronoUnit;
                    switch (ThreadLocalRandom.current().nextInt(1, 3)) {
                        case 1:
                            chronoUnit = ChronoUnit.SECONDS;
                            break;
                        case 2:
                            chronoUnit = ChronoUnit.MINUTES;
                            break;
                        default:
                            chronoUnit = ChronoUnit.HOURS;
                            break;
                    }

                    LocalDateTime lastDateTime = null;
                    for (int j = 0; j < kills; j++) {
                        lastDateTime = LocalDateTime.now().minus(ThreadLocalRandom.current()
                                .nextInt(0, 60), chronoUnit);
                        collectedPlayer.upKills(lastDateTime);
                    }
                    int deaths = ThreadLocalRandom.current().nextInt(0, 10);
                    for (int j = 0; j < deaths; j++) {
                        lastDateTime = LocalDateTime.now().minus(ThreadLocalRandom.current()
                                .nextInt(0, 60), chronoUnit);
                        collectedPlayer.upDeaths(lastDateTime);
                    }

                    if(k + 1 < sessions && Objects.nonNull(lastDateTime)) {
                        int secs = ThreadLocalRandom.current().nextInt(5, 30);
                        collectedPlayer.onDisconnected(lastDateTime.plusSeconds(secs));
                    }
                }

                if(collectedPlayer.getSessions().isEmpty()) {
                    ChronoUnit chronoUnit;
                    switch (ThreadLocalRandom.current().nextInt(1, 3)) {
                        case 1:
                            chronoUnit = ChronoUnit.SECONDS;
                            break;
                        case 2:
                            chronoUnit = ChronoUnit.MINUTES;
                            break;
                        default:
                            chronoUnit = ChronoUnit.HOURS;
                            break;
                    }

                    collectedPlayer.getCurrentSession(LocalDateTime.now()
                            .minus(ThreadLocalRandom.current().nextInt(0, 60), chronoUnit));
                }

                int ips = ThreadLocalRandom.current().nextInt(0, 1);
                for (int j = 0; j < ips; j++) {
                    int a = ThreadLocalRandom.current().nextInt(0, 255);
                    int b = ThreadLocalRandom.current().nextInt(0, 255);
                    int c = ThreadLocalRandom.current().nextInt(0, 255);
                    int d = ThreadLocalRandom.current().nextInt(0, 255);
                    collectedPlayer.getIpAddresses().add(a + "." + b + "." + c + "." + d);
                }
                int steamIds = ThreadLocalRandom.current().nextInt(0, 1);
                for (int j = 0; j < steamIds; j++) {
                    int id = ThreadLocalRandom.current().nextInt(100000, 100000000);
                    collectedPlayer.getSteamIds().add("STEAM_0:0:" + id);
                }

                gameSessions.put(randName, collectedPlayer);
            };

            gameSessionByAddress.put("127.0.0.1:" + port, gameSessions);
        }
    }
}