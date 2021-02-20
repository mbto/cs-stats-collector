package ru.csdm.stats.webapp.request;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.types.UInteger;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedCaseInsensitiveMap;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.webapp.DependentUtil;
import ru.csdm.stats.webapp.application.ChangesCounter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RequestScoped
@Named
@Slf4j
public class RequestDashboard {
    @Autowired
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private ChangesCounter changesCounter;
    @Autowired
    private DependentUtil util;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("\ninit");
    }

    public void onRowSelect(SelectEvent event) {
        Object object = event.getObject();

        if(log.isDebugEnabled())
            log.debug("\nonRowSelect " + object);

        String address = ((Map.Entry<String, CollectedPlayer>) object).getKey();

        util.sendRedirect("/sessions?address=" + address);
    }

    public Integer getSessionCount(String address) {
        Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(address);
        if(Objects.isNull(gameSessions))
            return null;

        return gameSessions.values()
                .stream()
                .mapToInt(cp -> cp.getSessions().size())
                .sum();
    }

    private static final Comparator<ServerData> cmpByProjectIdDesc = Comparator.comparing(o -> o.getKnownServer().getProjectId());
    private static final Comparator<ServerData> cmpByKnownServerId = Comparator.comparing(o -> o.getKnownServer().getId());

    public List<ServerData> getSortedServerData() {
        if(log.isDebugEnabled())
            log.debug("\ngetSortedServerData");

        return serverDataByAddress
                .values()
                .stream()
                .sorted(cmpByProjectIdDesc.reversed().thenComparing(cmpByKnownServerId))
                .collect(Collectors.toList());
    }

    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

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
                LocalDateTime lastDateTime = null;

                int sessions = ThreadLocalRandom.current().nextInt(1, 5);
                for (int k = 0; k < sessions; k++) {
                    int kills = ThreadLocalRandom.current().nextInt(0, 10);

                    ChronoUnit chronoUnit;
                    switch (ThreadLocalRandom.current().nextInt(1, 3)) {
                        case 1: chronoUnit = ChronoUnit.SECONDS; break;
                        case 2: chronoUnit = ChronoUnit.MINUTES; break;
                        default: chronoUnit = ChronoUnit.HOURS; break;
                    }

                    lastDateTime = null;
                    for (int j = 0; j < kills; j++) {
                        int amount = ThreadLocalRandom.current().nextInt(0, 60);
                        lastDateTime = LocalDateTime.now().minus(amount, chronoUnit);
                        collectedPlayer.upKills(lastDateTime);
                    }
                    int deaths = ThreadLocalRandom.current().nextInt(0, 10);
                    for (int j = 0; j < deaths; j++) {
                        int amount = ThreadLocalRandom.current().nextInt(0, 60);
                        if(Objects.isNull(lastDateTime)) {
                            lastDateTime = LocalDateTime.now().minus(amount, chronoUnit);
                        } else
                            lastDateTime = lastDateTime.plus(amount, chronoUnit);

                        collectedPlayer.upDeaths(lastDateTime);
                    }

                    if(k + 1 < sessions && Objects.nonNull(lastDateTime)) {
                        int secs = ThreadLocalRandom.current().nextInt(5, 30);
                        lastDateTime = lastDateTime.plusSeconds(secs);
                        collectedPlayer.onDisconnected(lastDateTime);
                    }
                }

                if(collectedPlayer.getSessions().isEmpty()) {
                    ChronoUnit chronoUnit;
                    switch (ThreadLocalRandom.current().nextInt(1, 3)) {
                        case 1: chronoUnit = ChronoUnit.SECONDS; break;
                        case 2: chronoUnit = ChronoUnit.MINUTES; break;
                        default: chronoUnit = ChronoUnit.HOURS; break;
                    }

                    lastDateTime = LocalDateTime.now()
                            .minus(ThreadLocalRandom.current().nextInt(0, 60), chronoUnit);
                    collectedPlayer.getCurrentSession(lastDateTime);
                }

                collectedPlayer.setLastseenDatetime(lastDateTime);

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
            }

            gameSessionByAddress.put("127.0.0.1:" + port, gameSessions);
        }
    }
}