package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedCaseInsensitiveMap;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.service.EventService;
import ru.csdm.stats.service.InstanceHolder;
import ru.csdm.stats.webapp.DependentUtil;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.BrokerEvent.FLUSH_FROM_FRONTEND;
import static ru.csdm.stats.common.utils.SomeUtils.forDashboardServerDataComparator;

@ViewScoped
@Named
@Slf4j
public class ViewDashboard {
    @Autowired
    private DependentUtil util;
    @Autowired
    private InstanceHolder instanceHolder;

    @Autowired
    private EventService eventService;
    @Autowired
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Getter private int processors;
    @Getter private String freeMemory;
    @Getter private String maxMemory;
    @Getter private String allocatedMemory;
    @Getter private String totalFreeMemory;
    @Getter private Integer instanceSessionsCount;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("\ninit");

        calculate();
        makeInstanceSessionsCount();
    }

    public void calculate() {
        Runtime runtime = Runtime.getRuntime();

        long freeMemoryL = runtime.freeMemory();
        long maxMemoryL = runtime.maxMemory();
        long allocatedMemoryL = runtime.totalMemory();

        processors = runtime.availableProcessors();
        freeMemory = String.format("%.2f", freeMemoryL / 1024f / 1024f);
        maxMemory = String.format("%.2f", maxMemoryL / 1024f / 1024f);
        allocatedMemory = String.format("%.2f", allocatedMemoryL / 1024f / 1024f);

        totalFreeMemory = String.format("%.2f", (freeMemoryL + (maxMemoryL - allocatedMemoryL)) / 1024f / 1024f);
    }

//    private static final Comparator<ServerData> cmpByProjectIdDesc = Comparator.comparing(o -> o.getKnownServer().getProjectId());
//    private static final Comparator<ServerData> cmpByKnownServerId = Comparator.comparing(o -> o.getKnownServer().getId());
    public List<ServerData> getSortedServerData() {
        if(log.isDebugEnabled())
            log.debug("\ngetSortedServerData");

        return serverDataByAddress
                .values()
                .stream()
                .sorted(forDashboardServerDataComparator)
//                .sorted(cmpByProjectIdDesc.reversed().thenComparing(cmpByKnownServerId))
                .collect(Collectors.toList());
    }

    public void onRowSelect(SelectEvent event) {
        Object object = event.getObject();

        if(log.isDebugEnabled())
            log.debug("\nonRowSelect " + object);

        String address = ((ServerData) object).getKnownServer().getIpport();

        util.sendRedirect("/sessions?address=" + address);
    }

    private void makeInstanceSessionsCount() {
        instanceSessionsCount = gameSessionByAddress.values()
                .stream()
                .mapToInt(gameSessions -> gameSessions
                        .values()
                        .stream()
                        .mapToInt(cp -> cp.getSessions().size())
                        .sum())
                .sum();
    }

    public Integer getSessionsCount(String address) {
        Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(address);
        if(Objects.isNull(gameSessions))
            return null;

        return gameSessions.values()
                .stream()
                .mapToInt(cp -> cp.getSessions().size())
                .sum();
    }

    public void flushInstanceSessions() {
        log.info("Flush all sessions received from frontend");

        List<String> infoMsgs = new ArrayList<>();
        List<String> warnMsgs = new ArrayList<>();

        for (String address : gameSessionByAddress.keySet()) {
            try {
                eventService.flushSessions(address, FLUSH_FROM_FRONTEND, false);
            } catch (Throwable e) {
                log.info(address + " Flush not registered, " + e.getMessage()); // info, not warn

                warnMsgs.add("Flush " + address + " not registered, " + e.getMessage());
                continue;
            }

            log.info(address + " Flush registered");

            infoMsgs.add("Flush " + address + " registered");
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        if(!infoMsgs.isEmpty())
            fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, String.join("<br/>", infoMsgs), ""));

        if(!warnMsgs.isEmpty())
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, String.join("<br/>", warnMsgs), ""));

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException ignored) {}

        makeInstanceSessionsCount();
    }

    public void refreshInstanceSettings() {
        log.info("Refresh instance settings received from frontend");

        FacesContext fc = FacesContext.getCurrentInstance();

        try {
            eventService.refreshSettings(null);
        } catch (Throwable e) {
            String msg = "Refresh not registered, " + e.getMessage();
            log.info(msg); // info, not warn

            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, msg, ""));
            return;
        }

        fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, "Refresh registered", ""));

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException ignored) {}

        makeInstanceSessionsCount();
    }

    public void makeFakes() {
        if(!instanceHolder.isDevEnvironment())
            return;

        for (String port : serverDataByAddress.keySet()) {
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

                collectedPlayer.setLastseenDatetime(Objects.isNull(lastDateTime)
                        ? LocalDateTime.now() : lastDateTime);

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

            gameSessionByAddress.put(port, gameSessions);
        }

        makeInstanceSessionsCount();
    }
}