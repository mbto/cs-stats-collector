package ru.csdm.stats.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Function;

import static ru.csdm.stats.common.Constants.YYYYMMDD_HHMMSS_PATTERN;

@Slf4j
public class DateUtils { //TODO: to remove
    /**
     * @param dateStr "2018-11-28"
     * @return java.time.LocalDate "2018-11-28"
     */
    public static LocalDate toLocalDate(String dateStr) {
        if(Objects.isNull(dateStr) || dateStr.isEmpty())
            return null;

        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            String message = "Failed convert String '" + dateStr + "' to LocalDate";
            log.warn(message, e);
            throw new RuntimeException(message, e);
        }
    }

    /**
     * @param dateTimeStr "2019-03-31 23:59:59"
     * @return java.sql.Date "2019-03-31"
     */
    public static java.sql.Date dateTimeToDate(String dateTimeStr) {
        if(Objects.isNull(dateTimeStr) || dateTimeStr.isEmpty())
            return null;

        try {
            return java.sql.Date.valueOf(LocalDate.parse(dateTimeStr, YYYYMMDD_HHMMSS_PATTERN));
        } catch (Exception e) {
            String message = "Failed convert String '" + dateTimeStr + "' to java.sql.Date";
            log.warn(message, e);
            throw new RuntimeException(message, e);
        }
    }

    /**
     * @param dateTimeStr "2019-03-31 23:59:59.999999"
     * @return java.sql.Date "2019-03-31"
     */
    public static java.sql.Date dateTimeMsToDate(String dateTimeStr) {
        if(Objects.isNull(dateTimeStr) || dateTimeStr.isEmpty())
            return null;

        try {
            LocalDate localDate = Timestamp.valueOf(dateTimeStr).toLocalDateTime().toLocalDate();
            return java.sql.Date.valueOf(localDate);
        } catch (Exception e) {
            String message = "Failed convert String '" + dateTimeStr + "' to java.sql.Date";
            log.warn(message, e);
            throw new RuntimeException(message, e);
        }
    }

    /**
     * @param timestampStr "2018-11-28 10:55:04.206523"
     * @return java.sql.Timestamp "2018-11-28 10:55:04.206523"
     */
    public static Timestamp toTimestamp(String timestampStr) {
        if(Objects.isNull(timestampStr) || timestampStr.isEmpty())
            return null;

        try {
            return Timestamp.valueOf(timestampStr);
        } catch (Exception e) {
            String message = "Failed convert String '" + timestampStr + "' to Timestamp";
            log.warn(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public static Function<String, LocalDate> strToLocalDate =
            rawDate -> LocalDate.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE);

    /**
     * Make begin MSK epoch timestamp with replaced time at 0 (for sql timestamp ranges)
     */
    public static Function<LocalDate, Timestamp> toBeginTimestamp = localDate -> Timestamp.valueOf(
            localDate.atTime(0, 0, 0, 0));

    /**
     * Make end MSK epoch timestamp with replaced time at 23:59:59:999999 (for sql timestamp ranges)
     */
    public static Function<LocalDate, Timestamp> toEndTimestamp = localDate -> Timestamp.valueOf(
            localDate.atTime(23, 59, 59, 999999000));
}