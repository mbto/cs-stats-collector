package ru.csdm.stats.modules.collector.handlers;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.FlushEvent;
import ru.csdm.stats.common.GameSessionFetchMode;
import ru.csdm.stats.common.SystemEvent;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.DatagramsQueue;
import ru.csdm.stats.common.dto.Message;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.model.tables.pojos.KnownServer;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static ru.csdm.stats.common.Constants.MMDDYYYY_HHMMSS_PATTERN;
import static ru.csdm.stats.common.FlushEvent.*;
import static ru.csdm.stats.common.GameSessionFetchMode.*;
import static ru.csdm.stats.modules.collector.settings.Patterns.*;

@Service
@Lazy(false)
@Slf4j
public class DatagramsConsumer {
    @Autowired
    private ThreadPoolTaskExecutor consumerTaskExecutor;

    @Autowired
    private Map<String, ServerData> availableAddresses;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Autowired
    private PlayersSender playersSender;

    private CountDownLatch deactivationLatch;

    @Getter
    @Setter /* Setter - allowing calling from another class/thread, with spring proxy, without volatile */
    private boolean deactivated;

    @PreDestroy
    public void destroy() {
        boolean debugEnabled = log.isDebugEnabled();
        if(debugEnabled)
            log.debug("destroy() start");

        setDeactivated(true);

        int poolSize = consumerTaskExecutor.getPoolSize();
        deactivationLatch = new CountDownLatch(poolSize);

        ThreadGroup tg = consumerTaskExecutor.getThreadGroup();
        if(Objects.nonNull(tg)) {
            Thread[] consumers = new Thread[poolSize];
            tg.enumerate(consumers);

            if(consumers.length > 0) {
                if(debugEnabled)
                    log.debug("Interrupting " + consumers.length +
                            " consumer" + (consumers.length > 1 ? "s" : "") + "...");

                tg.interrupt();
            }
        }

        try {
            deactivationLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        for (String address : gameSessionByAddress.keySet()) {
            flushSessions(address, null, PRE_DESTROY_LIFECYCLE);
        }

        if(debugEnabled)
            log.debug("destroy() end");
    }

    @Async("consumerTaskExecutor")
    public void startConsumeAsync(DatagramsQueue datagramsQueue) {
        log.info("Activating DatagramsConsumer");

        while (true) {
            boolean debugEnabled = log.isDebugEnabled();

            Message message;

            try {
                if (isDeactivated()) {
                    log.info("Deactivation detected");
                    break;
                }

                if(debugEnabled)
                    log.debug("Waiting message from datagramsQueue...");

                message = datagramsQueue.getDatagramsQueue().takeFirst();

                if(debugEnabled)
                    log.debug(message.getServerData().getKnownServer().getIpport() + " Taked message: " + message);
            } catch (Throwable e) {
                if (isDeactivated()) {
                    log.info("Deactivation detected");
                    break;
                }

                log.warn("Exception while receiving message", e);
                continue;
            }

            ServerData serverData = message.getServerData();
            KnownServer knownServer = serverData.getKnownServer();
            String address = knownServer.getIpport();

            if(Objects.nonNull(message.getSystemEvent())) {
                if(debugEnabled)
                    log.debug(address + " Taked system event: " + message.getSystemEvent());

                if(message.getSystemEvent() == SystemEvent.FLUSH_SESSIONS) {
                    flushSessions(address, null, ENDPOINT);
                }

                continue;
            }

            Matcher msgMatcher = LOG.pattern.matcher(message.getPayload());

            if (!msgMatcher.find())
                continue;

            LocalDateTime dateTime = LocalDateTime.parse(msgMatcher.group("date"), MMDDYYYY_HHMMSS_PATTERN);
            serverData.setLastTouchDateTime(dateTime);

/* L 01/01/2020 - 20:50:08: Started map "de_dust2" (CRC "1159425449") -> Started map "de_dust2" (CRC "1159425449") */
            String rawMsg = msgMatcher.group("msg");
            Matcher actionMatcher = TWO.pattern.matcher(rawMsg);

            if (actionMatcher.find()) {
                String eventName = actionMatcher.group(2);

/* L 01/01/2020 - 13:15:02: "Name1<5><STEAM_ID_LAN><CT>" killed "Name2<6><STEAM_ID_LAN><T>" with "m4a1" */
                if (eventName.equals("killed")) {
                    String sourceRaw = actionMatcher.group(1);
                    String targetRaw = actionMatcher.group(3);

                    Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                    Matcher targetMatcher = PLAYER.pattern.matcher(targetRaw);

                    if (sourceMatcher.find() && targetMatcher.find()) {
                        String killerAuth = sourceMatcher.group("auth");
                        String victimAuth = targetMatcher.group("auth");

                        if(knownServer.getIgnoreBots()) {
                            if("BOT".equals(killerAuth) || "BOT".equals(victimAuth)) {
                                if(debugEnabled) {
                                    log.debug(address + " Skip BOT frag: " + sourceRaw + " or " + targetRaw);
                                }

                                continue;
                            }
                        }

                        String killerName = sourceMatcher.group("name");
                        String victimName = targetMatcher.group("name");

                        if(knownServer.getFfa()) {
                            countFrag(knownServer, dateTime, killerName, killerAuth, victimName, victimAuth);
                        } else {
                            String killerTeam = sourceMatcher.group("team");
                            String victimTeam = targetMatcher.group("team");

                            if(StringUtils.isNotBlank(killerTeam)
                                    && StringUtils.isNotBlank(victimTeam)
                                    && !StringUtils.equals(killerTeam, victimTeam)
                            ) {
                                countFrag(knownServer, dateTime, killerName, killerAuth, victimName, victimAuth);
                            }
                        }

                        continue;
                    }

                    continue;
                }

                continue;
            }

            Matcher eventMatcher = THREE.pattern.matcher(rawMsg);
            if(eventMatcher.find()) {
                String eventName = eventMatcher.group(2);

/* L 01/01/2020 - 13:15:00: "Name1<5><STEAM_ID_LAN><>" connected, address "12.12.12.12:27005" */
                if(eventName.equals("connected, address")) { // for players + some bots
                    String sourceRaw = eventMatcher.group(1);
                    Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                    if (sourceMatcher.matches()) {
                        String sourceAuth = sourceMatcher.group("auth");

                        if (knownServer.getIgnoreBots()) {
                            // some bots generates event "connected, address"
                            if ("BOT".equals(sourceAuth)) {
                                continue;
                            }
                        }

                        String sourceName = sourceMatcher.group("name");
                        String sourceIp = eventMatcher.group(3); // Possible values: "loopback:27005", "12.12.12.12:27005", "none"

                        CollectedPlayer collectedPlayer = allocatePlayer(knownServer, sourceName, sourceAuth, dateTime);
                        collectedPlayer.addIpAddress(sourceIp);

                        if(knownServer.getStartSessionOnAction()) {
                            continue;
                        }

                        collectedPlayer.getCurrentSession(dateTime); // activate session
                        continue;
                    }

                    continue;
                }

                if(eventName.equals("committed suicide with")) {
                    String sourceRaw = eventMatcher.group(1);
                    Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                    if (sourceMatcher.matches()) {
                        String sourceAuth = sourceMatcher.group("auth");

                        if (knownServer.getIgnoreBots()) {
                            if ("BOT".equals(sourceAuth)) {
                                if(debugEnabled) {
                                    log.debug(address + " Skip BOT suicide: " + sourceRaw);
                                }

                                continue;
                            }
                        }

                        String sourceName = sourceMatcher.group("name");

                        CollectedPlayer collectedPlayer = allocatePlayer(knownServer, sourceName, sourceAuth, dateTime);
                        collectedPlayer.upDeaths(dateTime);

                        continue;
                    }

                    continue;
                }

                if(eventName.equals("changed name to")) {
                    String sourceRaw = eventMatcher.group(1);
                    Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                    if (sourceMatcher.matches()) {
                        String sourceAuth = sourceMatcher.group("auth");

                        if (knownServer.getIgnoreBots()) {
                            if ("BOT".equals(sourceAuth)) {
                                if(debugEnabled) {
                                    log.debug(address + " Skip BOT changed name: " + sourceRaw);
                                }

                                continue;
                            }
                        }

                        String sourceName = sourceMatcher.group("name");
                        String sourceNewName = eventMatcher.group(3);

                        CollectedPlayer collectedPlayer = allocatePlayer(knownServer, sourceName, sourceAuth, dateTime);
                        collectedPlayer.onDisconnected(dateTime);

                        Set<String> ipAddresses = collectedPlayer.getIpAddresses();

                        collectedPlayer = allocatePlayer(knownServer, sourceNewName, sourceAuth, dateTime);
                        for (String ipAddress : ipAddresses) {
                            collectedPlayer.addIpAddress(ipAddress);
                        }

                        if(knownServer.getStartSessionOnAction()) {
                            continue;
                        }

                        collectedPlayer.getCurrentSession(dateTime); // activate session
                        continue;
                    }

                    continue;
                }

                continue;
            }

            eventMatcher = FOUR.pattern.matcher(rawMsg);
            if (eventMatcher.find()) {
                String eventName = eventMatcher.group(2);

/* L 01/01/2020 - 20:50:20: "timoxatw<3><BOT><>" entered the game */
                if (eventName.equals("entered the game")) { // for players + all bots
                    String sourceRaw = eventMatcher.group(1);
                    Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                    if (sourceMatcher.matches()) {
                        String sourceAuth = sourceMatcher.group("auth");

                        if(knownServer.getIgnoreBots()) {
                            if("BOT".equals(sourceAuth)) {
                                continue;
                            }
                        }

                        String sourceName = sourceMatcher.group("name");
                        CollectedPlayer collectedPlayer = allocatePlayer(knownServer, sourceName, sourceAuth, dateTime);

                        if(knownServer.getStartSessionOnAction()) {
                            continue;
                        }

                        collectedPlayer.getCurrentSession(dateTime); // activate session
                        continue;
                    }

                    continue;
                }

/* L 01/01/2020 - 20:52:10: "timoxatw<3><BOT><TERRORIST>" disconnected */
                if (eventName.equals("disconnected")) {
                    String sourceRaw = eventMatcher.group(1);
                    Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                    if (sourceMatcher.find()) {
                        String sourceName = sourceMatcher.group("name");

                        Map<String, CollectedPlayer> gameSessions = allocateGameSession(address, DONT_CREATE);

                        if(Objects.nonNull(gameSessions)) {
                            CollectedPlayer collectedPlayer = gameSessions.get(sourceName);

                            if (Objects.nonNull(collectedPlayer)) {
                                collectedPlayer.onDisconnected(dateTime);
                                collectedPlayer.setLastseenDatetime(dateTime);
                            }
                        }

                        continue;
                    }

                    continue;
                }

                continue;
            }

            eventMatcher = SIX.pattern.matcher(rawMsg);
            if (eventMatcher.find()) {
                String eventName = eventMatcher.group(1);

/* L 01/01/2020 - 20:50:08: Started map "de_dust2" (CRC "1159425449") */
                if (eventName.equals("Started map")) {
                    flushSessions(address, dateTime, NEW_GAME_MAP);
                    continue;
                }

                continue;
            }

/* L 01/01/2020 - 20:52:15: Server shutdown */
            if(rawMsg.equals("Server shutdown")) {
                flushSessions(address, dateTime, SHUTDOWN_GAME_SERVER);
                continue;
            }
        }

        if(Objects.nonNull(deactivationLatch))
            deactivationLatch.countDown();

        log.info("Deactivated");
    }

    private void countFrag(KnownServer knownServer,
                           LocalDateTime dateTime,
                           String killerName, String killerAuth,
                           String victimName, String victimAuth) {
        CollectedPlayer killer = allocatePlayer(knownServer, killerName, killerAuth, dateTime);
        killer.upKills(dateTime);

        CollectedPlayer victim = allocatePlayer(knownServer, victimName, victimAuth, dateTime);
        victim.upDeaths(dateTime);
    }

    private Map<String, CollectedPlayer> allocateGameSession(String address, GameSessionFetchMode gsFetchMode) {
        if(gsFetchMode == DONT_CREATE) {
            return gameSessionByAddress.get(address);
        }
        else if(gsFetchMode == CREATE_IF_NULL) {
            Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(address);

            if (Objects.isNull(gameSessions)) {
                gameSessions = new LinkedHashMap<>();
                gameSessionByAddress.put(address, gameSessions);

                log.info(address + " Created gameSessions container");
            }

            return gameSessions;
        }
        else if(gsFetchMode == REPLACE_IF_EXISTS) {
            Map<String, CollectedPlayer> oldGameSessions = gameSessionByAddress
                    .replace(address, new LinkedHashMap<>());

            if(Objects.nonNull(oldGameSessions))
                log.info(address + " Recreated gameSessions container");

            return oldGameSessions; /* return old container, for flush */
        }
        else if(gsFetchMode == REMOVE) {
            return gameSessionByAddress.remove(address); /* return old container, for flush */
        }

        throw new IllegalStateException("Allocation mode '" + gsFetchMode + "' not chosen");
    }

    private CollectedPlayer allocatePlayer(KnownServer knownServer, String name,
                                           String steamId, LocalDateTime dateTime) {
        String address = knownServer.getIpport();
        Map<String, CollectedPlayer> gameSessions = allocateGameSession(address, CREATE_IF_NULL);

        CollectedPlayer collectedPlayer = gameSessions.get(name);

        if (Objects.isNull(collectedPlayer)) {
            collectedPlayer = new CollectedPlayer(name);
            gameSessions.put(name, collectedPlayer);

            log.info(address + " Founded player: " + collectedPlayer);
        }

        // I will try adding steamId to the HashSet on every call, since the cs-stats-collector can be
        // started later, after the players have joined. If this happens, the players' IPs will remain
        // unknown, but at least steamId will remain.
        collectedPlayer.addSteamId(steamId);

        // Set on every call, if the known server ID suddenly changes (for example, when
        // calling POST /updateSettings and changing the known server ID, but it is unlikely
        // that you want to change the known server ID at runtime)
        collectedPlayer.setLastServerId(knownServer.getId());

        collectedPlayer.setLastseenDatetime(dateTime);

        return collectedPlayer;
    }

    private void flushSessions(String address, LocalDateTime dateTime, FlushEvent fromEvent) {
        Map<String, CollectedPlayer> gameSessions = allocateGameSession(address,
                fromEvent != PRE_DESTROY_LIFECYCLE ? REPLACE_IF_EXISTS : REMOVE);

        if(Objects.isNull(gameSessions)) {
            log.info(address + " Skip flushing players, due gameSessions container not exists. " + fromEvent);
            return;
        }

        List<CollectedPlayer> collectedPlayers = new ArrayList<>(gameSessions.values());

        int playersSize = collectedPlayers.size();
        if (playersSize == 0) {
            log.info(address + " Skip flushing players, due empty players container. " + fromEvent);
            return;
        }

        log.info(address + " Prepared " + playersSize +
                " player" + (playersSize > 1 ? "s" : "") + " to flush. " + fromEvent);

        if(Objects.isNull(dateTime)) {
            dateTime = availableAddresses.get(address).getLastTouchDateTime();
        }

        for (CollectedPlayer collectedPlayer : collectedPlayers) {
            collectedPlayer.prepareToFlushSessions(dateTime);
        }

        playersSender.sendAsync(address, collectedPlayers);
    }
}