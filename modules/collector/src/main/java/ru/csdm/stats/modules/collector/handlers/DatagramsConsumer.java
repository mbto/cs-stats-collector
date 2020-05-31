package ru.csdm.stats.modules.collector.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.FlushEvent;
import ru.csdm.stats.common.dto.*;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static ru.csdm.stats.common.Constants.MMDDYYYY_HHMMSS_PATTERN;
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
    private Map<String, Map<String, Player>> gameSessionByAddress;

    @Autowired
    private PlayersSender playersSender;

    private CountDownLatch deactivationLatch;

    private boolean deactivated;

    @PreDestroy
    public void destroy() {
        boolean debugEnabled = log.isDebugEnabled();
        if(debugEnabled)
            log.debug("destroy() start");

        deactivated = true;

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
            flushSessions(address, null, FlushEvent.PRE_DESTROY_LIFECYCLE);
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
                if (deactivated) {
                    log.info("Deactivation detected");
                    break;
                }

                if(debugEnabled)
                    log.debug("Waiting message from datagramsQueue...");

                message = datagramsQueue.getDatagramsQueue().takeFirst();

                if(debugEnabled)
                    log.debug(message.getServerData().getServerSetting().getIpport() + " Taked message: " + message);
            } catch (Throwable e) {
                if (deactivated) {
                    log.info("Deactivation detected");
                    break;
                }

                log.warn("Exception while receiving message", e);
                continue;
            }

            Matcher msgMatcher = LOG.pattern.matcher(message.getPayload());

            if (!msgMatcher.find())
                continue;

            LocalDateTime dateTime = LocalDateTime.parse(msgMatcher.group("date"), MMDDYYYY_HHMMSS_PATTERN);

            ServerData serverData = message.getServerData();
            serverData.setLastTouchDateTime(dateTime);

            ServerSetting serverSetting = serverData.getServerSetting();
            String address = serverSetting.getIpport();

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
                        String killerName = sourceMatcher.group("name");
                        String victimName = targetMatcher.group("name");

                        if(serverSetting.getIgnore_bots()) {
                            String killerAuth = sourceMatcher.group("auth");
                            String victimAuth = targetMatcher.group("auth");

                            if("BOT".equals(killerAuth) || "BOT".equals(victimAuth)) {
                                if(debugEnabled) {
                                    log.debug(address + " Skip BOT frag: " + sourceRaw + " & " + targetRaw);
                                }

                                continue;
                            }
                        }

                        if(serverSetting.getFfa()) {
                            countFrag(address, dateTime, killerName, victimName);
                        } else {
                            String killerTeam = sourceMatcher.group("team");
                            String victimTeam = targetMatcher.group("team");

                            if(StringUtils.isNotBlank(killerTeam)
                                    && StringUtils.isNotBlank(victimTeam)
                                    && !StringUtils.equals(killerTeam, victimTeam)
                            ) {
                                countFrag(address, dateTime, killerName, victimName);
                            }
                        }
                    }

                    continue;
                }

                continue;
            }

            Matcher eventMatcher = FOUR.pattern.matcher(rawMsg);
            if (eventMatcher.find()) {
                String eventName = eventMatcher.group(2);

/* L 01/01/2020 - 20:50:20: "timoxatw<3><BOT><>" entered the game */
                if (eventName.equals("entered the game")) {
                    if(serverSetting.getStart_session_on_action()) {
                        continue;
                    }

                    String sourceRaw = eventMatcher.group(1);
                    Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                    if (sourceMatcher.matches()) {
                        if(serverSetting.getIgnore_bots()) {
                            String sourceAuth = sourceMatcher.group("auth");

                            if("BOT".equals(sourceAuth)) {
                                continue;
                            }
                        }

                        String sourceName = sourceMatcher.group("name");
                        allocatePlayer(address, sourceName);

                        //address "12.12.12.12:27005"
                        //address "loopback:27005"
                        /*sourceMatcher = PLAYER_IP.pattern.matcher(eventName);
                        if(sourceMatcher.find()) {
                            String sourceIp = sourceMatcher.group();
                        }*/
                    }

                    continue;
                }

/* L 01/01/2020 - 20:52:10: "timoxatw<3><BOT><TERRORIST>" disconnected */
                if (eventName.equals("disconnected")) {
                    String sourceRaw = eventMatcher.group(1);
                    Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                    if (sourceMatcher.find()) {
                        String sourceName = sourceMatcher.group("name");

                        Map<String, Player> gameSessions = allocateGameSession(address, false);

                        if(Objects.nonNull(gameSessions)) {
                            Player player;
                            synchronized (gameSessions) {
                                player = gameSessions.get(sourceName);
                            }

                            if (Objects.nonNull(player)) {
                                player.onDisconnected(dateTime);
                            }
                        }
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
                    flushSessions(address, dateTime, FlushEvent.NEW_GAME_MAP);
                    continue;
                }

                continue;
            }

/* L 01/01/2020 - 20:52:15: Server shutdown */
            if(rawMsg.equals("Server shutdown")) {
                flushSessions(address, dateTime, FlushEvent.SHUTDOWN_GAME_SERVER);
                continue;
            }
        }

        if(Objects.nonNull(deactivationLatch))
            deactivationLatch.countDown();

        log.info("Deactivated");
    }

    private void countFrag(String address, LocalDateTime dateTime, String killerName, String victimName) {
        Player killer = allocatePlayer(address, killerName);
        killer.upKills(dateTime);

        Player victim = allocatePlayer(address, victimName);
        victim.upDeaths(dateTime);
    }

    private Map<String, Player> allocateGameSession(String address, boolean createIfNotExists) {
        Map<String, Player> gameSessions = gameSessionByAddress.get(address);
        if(Objects.isNull(gameSessions) && createIfNotExists) {
            gameSessions = new LinkedHashMap<>();
            gameSessionByAddress.put(address, gameSessions);

            log.info(address + " Created gameSessions container");
        }

        return gameSessions;
    }

    private Player allocatePlayer(String address, String name) {
        Map<String, Player> gameSessions = allocateGameSession(address, true);

        Player player;
        synchronized (gameSessions) {
            player = gameSessions.get(name);

            if (Objects.isNull(player)) {
                player = new Player(name);
                gameSessions.put(name, player);

                log.info(address + " Founded player: " + player);
            }
        }

        return player;
    }

    public void flushSessions(String address, LocalDateTime dateTime, FlushEvent fromEvent) {
        Map<String, Player> gameSessions = gameSessionByAddress.get(address);

        if(Objects.isNull(gameSessions)) {
            log.info(address + " Skip flushing players, due gameSessions container not exists. " + fromEvent);
            return;
        }

        List<Player> players;
        synchronized (gameSessions) { // synchronizing, due flushSession can invoked from /flush endpoint
            players = new ArrayList<>(gameSessions.values());
            gameSessions.clear();
        }

        int playersSize = players.size();
        if (playersSize == 0) {
            log.info(address + " Skip flushing players, due empty players container. " + fromEvent);
            return;
        }

        log.info(address + " Prepared " + playersSize +
                " player" + (playersSize > 1 ? "s" : "") + " to flush. " + fromEvent);

        if(Objects.isNull(dateTime)) {
            dateTime = availableAddresses.get(address).getLastTouchDateTime();
        }

        for (Player player : players) {
            player.prepareToFlushSessions(dateTime);
        }

        playersSender.sendAsync(address, players);
    }
}