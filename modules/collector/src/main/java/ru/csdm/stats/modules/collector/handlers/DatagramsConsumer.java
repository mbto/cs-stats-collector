package ru.csdm.stats.modules.collector.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import ru.csdm.stats.common.FlushEvent;
import ru.csdm.stats.common.GameSessionFetchMode;
import ru.csdm.stats.common.SystemEvent;
import ru.csdm.stats.common.dto.*;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.dao.CollectorDao;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
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
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Autowired
    private PlayersSender playersSender;

    @PreDestroy
    public void destroy() {
        if(log.isDebugEnabled())
            log.debug("destroy() start");

        if(log.isDebugEnabled())
            log.debug("destroy() end");
    }

    @Async("consumerTE")
    public void startConsumeAsync(MessageQueue messageQueue) {
        log.info(messageQueue + " Activating");

        while (true) {
            boolean debugEnabled = log.isDebugEnabled();

            Message<?> message;
            try {
                if(debugEnabled)
                    log.debug(messageQueue + " Waiting message");

                message = messageQueue.getMessageQueue().takeFirst();

                if(debugEnabled)
                    log.debug(messageQueue + " Taked message: " + message);
            } catch (Throwable e) {
                log.warn("Exception while takeFirst message", e);
                continue;
            }

            if(Objects.nonNull(message.getSystemEvent())) {
                SystemEvent systemEvent = message.getSystemEvent();
                log.info(messageQueue + " Taked system event: " + systemEvent);

                if(systemEvent == SystemEvent.REFRESH) {
                    CyclicBarrier cb = (CyclicBarrier) message.getPojo();
                    if(cb.isBroken()) {
                        log.info(messageQueue + " Cancel synchronization");
                        continue;
                    }

                    log.info(messageQueue + " Ready synchronization");

                    try {
                        cb.await();
                    } catch (Throwable e) {
                        log.warn(messageQueue + " Exception while await synchronization", e);
                        continue;
                    }

                    log.info(messageQueue + " End synchronization");
                } else if(systemEvent == SystemEvent.FLUSH_FROM_FRONTEND) {
                    flushSessions((ServerData) message.getPojo(), FRONTEND);
                } else if(systemEvent == SystemEvent.FLUSH_FROM_SCHEDULER) {
                    flushSessions((ServerData) message.getPojo(), SCHEDULER);
                } else if(systemEvent == SystemEvent.QUIT) {
                    break;
                } else if(systemEvent == SystemEvent.FLUSH_AND_QUIT) {
                    for (String port : messageQueue.getKnownServersPorts()) {
                        flushSessions(serverDataByAddress.get(port), PRE_DESTROY_LIFECYCLE);
                    }

                    break;
                }

                continue;
            }

            Matcher msgMatcher = LOG.pattern.matcher(message.getPayload());

            if(!msgMatcher.find())
                continue;

            ServerData serverData = (ServerData) message.getPojo();
            KnownServer knownServer = serverData.getKnownServer();
            String address = knownServer.getIpport();

            LocalDateTime dateTime = LocalDateTime.parse(msgMatcher.group("date"), MMDDYYYY_HHMMSS_PATTERN);
            serverData.setLastTouchDateTime(dateTime);

            // Extract "msg" from "L 01/01/2020 - 20:50:08: msg"
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

/* L 01/01/2020 - 21:19:26: "Currv<29><BOT><CT>" committed suicide with "grenade" */
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

/* L 01/01/2020 - 13:15:08: "Name5<5><STEAM_0:0:123456><CT>" changed name to "Name9" */
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

                        /* Using compare method from org.springframework.util.LinkedCaseInsensitiveMap#convertKey */
                        if(sourceName.toLowerCase().equals(sourceNewName.toLowerCase())) {
                            // changing name from Source to source, nothing changes
                            collectedPlayer.setName(sourceNewName);
                            // TODO: test
//                            Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(address);
//                            if(Objects.nonNull(gameSessions) && !gameSessions.containsKey(sourceName)) {
//                                gameSessions.put(sourceNewName, gameSessions.remove(sourceName));
//                            }
                        } else {
                            collectedPlayer.onDisconnected(dateTime);

                            Set<String> ipAddresses = collectedPlayer.getIpAddresses();

                            collectedPlayer = allocatePlayer(knownServer, sourceNewName, sourceAuth, dateTime);
                            // without ru.csdm.stats.common.dto.CollectedPlayer#addIpAddress, due already extracted
                            collectedPlayer.getIpAddresses().addAll(ipAddresses);

                            if (knownServer.getStartSessionOnAction()) {
                                continue;
                            }

                            collectedPlayer.getCurrentSession(dateTime); // activate session
                        }

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
                if(eventName.equals("Started map")) {
                    flushSessions(serverData, dateTime, NEW_GAME_MAP);
                    continue;
                }

                continue;
            }

            // Did not match patterns:

/* L 01/01/2020 - 20:52:15: Server shutdown */
            if(rawMsg.equals("Server shutdown")) {
                flushSessions(serverData, dateTime, SHUTDOWN_GAME_SERVER);
                continue;
            }
        }

//        if(Objects.nonNull(deactivationLatch))
//            deactivationLatch.countDown();

        log.info(messageQueue + " Deactivated");
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
                gameSessions = new LinkedCaseInsensitiveMap<>(); /* Player names registry */
                gameSessionByAddress.put(address, gameSessions);

                log.info(address + " Created gameSessions registry");
            }

            return gameSessions;
        }
        else if(gsFetchMode == REPLACE_IF_EXISTS) {
            Map<String, CollectedPlayer> oldGameSessions = gameSessionByAddress
                    .replace(address, new LinkedCaseInsensitiveMap<>()); /* Player names registry */

            if(Objects.nonNull(oldGameSessions))
                log.info(address + " Recreated gameSessions registry");

            return oldGameSessions; /* return old registry, for flush */
        }
        else if(gsFetchMode == REMOVE) {
            return gameSessionByAddress.remove(address); /* return old registry, for flush */
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

        collectedPlayer.setLastseenDatetime(dateTime);

        return collectedPlayer;
    }

    private void flushSessions(ServerData serverData, FlushEvent fromEvent) {
        flushSessions(serverData, null, fromEvent);
    }

    private void flushSessions(ServerData serverData, LocalDateTime dateTime, FlushEvent fromEvent) {
        String address = serverData.getKnownServer().getIpport();

        String logMsg = "Started flush sessions by event " + fromEvent;
        log.info(address + " " + logMsg);
        serverData.addMessage(logMsg);

        Map<String, CollectedPlayer> gameSessions = allocateGameSession(address,
                (fromEvent == PRE_DESTROY_LIFECYCLE /*|| */)
                        ? REMOVE : REPLACE_IF_EXISTS);

        if(Objects.isNull(gameSessions) || gameSessions.isEmpty()) {
            logMsg = "Skip flush sessions, due empty gameSessions registry or not exists";
            log.info(address + " " + logMsg);
            serverData.addMessage(logMsg);
            return;
        }

        List<CollectedPlayer> collectedPlayers = new ArrayList<>(gameSessions.values());

        int playersSize = collectedPlayers.size();
        /*if (playersSize == 0) { //todo: why I'm write this?
            logMsg = "Skip flush sessions, due empty collectedPlayers registry";
            log.info(address + " " + logMsg);
            serverData.addMessage(logMsg);
            return;
        }*/

        logMsg = "Prepared " + playersSize + " player" + (playersSize > 1 ? "s" : "") + " to flush";
        log.info(address + " " + logMsg);
        serverData.addMessage(logMsg);

        if(Objects.isNull(dateTime)) {
            dateTime = serverData.getLastTouchDateTime();
        }

        serverData.setNextFlushDateTime(dateTime.plusHours(1));

        for (CollectedPlayer collectedPlayer : collectedPlayers) {
            collectedPlayer.prepareToFlushSessions(dateTime);
        }

        // after this call use of registry in sendAsync onwards may be outdated
        playersSender.sendAsync(serverData, collectedPlayers);
    }
}