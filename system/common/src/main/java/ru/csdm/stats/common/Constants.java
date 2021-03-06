package ru.csdm.stats.common;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public interface Constants {
    DateTimeFormatter YYYYMMDD_HHMMSS_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat YYYYMMDD_HHMMSS_OLD_PATTERN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat YYYYMMDD2_OLD_PATTERN = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat YYYYMMDD_OLD_PATTERN = new SimpleDateFormat("yyyy-MM-dd");
    DateTimeFormatter YYYYMMDD_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter DDMMYYYY_PATTERN = DateTimeFormatter.ofPattern("ddMMyyyy");
    DateTimeFormatter MMDDYYYY_HHMMSS_PATTERN = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
    Pattern IPADDRESS_PATTERN = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
    Pattern STEAMID_PATTERN = Pattern.compile("STEAM_[0-1]:[0-1]:[0-9]+");
}