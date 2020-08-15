package ru.csdm.stats.common.utils;

import org.apache.commons.lang3.StringUtils;
import ru.csdm.stats.common.model.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.tables.pojos.Player;
import ru.csdm.stats.common.model.tables.records.PlayerRecord;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static ru.csdm.stats.common.Constants.IPADDRESS_PATTERN;
import static ru.csdm.stats.common.Constants.STEAMID_PATTERN;

public class SomeUtils {

    public static String extractIp(String ipRaw) {
        if(StringUtils.isBlank(ipRaw))
            return null;

        Matcher matcher = IPADDRESS_PATTERN.matcher(ipRaw);
        if(matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    public static String extractSteamId(String steamIdRaw) {
        if(StringUtils.isBlank(steamIdRaw))
            return null;

        Matcher matcher = STEAMID_PATTERN.matcher(steamIdRaw);
        if(matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    public static String knownServerToString(KnownServer knownServer) {
        return knownServer.getIpport() + ": ffa=" + knownServer.getFfa()
                + ", ignore_bots=" + knownServer.getIgnoreBots()
                + ", start_session_on_action=" + knownServer.getStartSessionOnAction();
    }

    public static String playerToString(Player player) {
        return player.getName() + ": kills=" + player.getKills() +
                ", deaths=" + player.getDeaths() +
                ", time=" + player.getTimeSecs() + "s" +
                " (" + SomeUtils.humanLifetime(player.getTimeSecs().longValue() * 1000) + ")";
    }

    public static String playerRecordToString(PlayerRecord playerRecord) {
        return playerToString(playerRecord.into(Player.class));
    }

    public static String addressToString(SocketAddress sa) {
        InetSocketAddress isa = (InetSocketAddress) sa;
        return isa.getAddress().getHostAddress() + ":" + isa.getPort();
    }

    public static String humanFileSize(long size) {
        return String.format("%.2f", (size / 1024f / 1024f)) + "mb";
    }

    public static String humanLifetime(Timestamp dateStart, Timestamp dateEnd) {
        return humanLifetime(dateStart.toLocalDateTime(),
                Optional.ofNullable(dateEnd)
                        .map(Timestamp::toLocalDateTime)
                        .orElse(null));
    }

    public static String humanLifetime(LocalDateTime dateStart, LocalDateTime dateEnd) {
        Duration duration = Duration.between(dateStart,
                Objects.nonNull(dateEnd) ? dateEnd : LocalDateTime.now());

        long millis = duration.toMillis();
        return humanLifetime(millis);
    }

    public static String humanLifetime(long millis) {
        long hrs = TimeUnit.MILLISECONDS.toHours(millis);
        long mins = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hrs);
        long secs = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        StringBuilder sb = new StringBuilder();
        if(hrs > 0)
            sb.append(String.format("%dh ", hrs));
        if(mins > 0)
            sb.append(String.format("%dm ", mins));

        sb.append(String.format("%ds", secs));
        return sb.toString();
    }

    /**
     * Разбить все элементы List'а поровну на partsCount List'ов
     * @param list исходный List
     * @param partsCount количество ожидаемых частей
     * @return List<List<T>>
     */
    public static <T> List<List<T>> partition(List<T> list, int partsCount) {
        partsCount = (int) Math.ceil(list.size() / (float) partsCount);

        List<List<T>> result = new ArrayList<>();
        for (int start = 0; start < list.size(); start += partsCount) {
            result.add(list.subList(start, Math.min(start + partsCount, list.size())));
        }

        return result;
    }

    /**
     * Разбить любой text разделителем delimeter ровно на fixedReturnedSize элементов
     * @param text строка
     * @param delimeter разделитель
     * @param fixedReturnedSize фиксированный размер возвращаемой коллекции
     * @return List<String> размером fixedReturnedSize
     */
    public static List<String> fixedSplit(String text, String delimeter, int fixedReturnedSize) {
        List<String> result = new ArrayList<>();
        int lastPos = -1;
        for (int i = 0; i < fixedReturnedSize; i++) {
            if(lastPos == text.length()) {
                result.add("");
                continue;
            }

            int pos1 = 0;
            if(lastPos != -1) {
                pos1 = lastPos + delimeter.length();
            }

            int pos2 = text.indexOf(delimeter, lastPos + delimeter.length());
            if(pos2 == -1)
                pos2 = text.length();

            result.add(text.substring(pos1, pos2));
            lastPos = pos2;
        }

        return result;
    }
}
