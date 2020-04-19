package ru.csdm.stats.common;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

public interface Constants {
    DateTimeFormatter YYYYMMDD_HHMMSS_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat YYYYMMDD_HHMMSS_OLD_PATTERN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat YYYYMMDD2_OLD_PATTERN = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat YYYYMMDD_OLD_PATTERN = new SimpleDateFormat("yyyy-MM-dd");
    DateTimeFormatter YYYYMMDD_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter DDMMYYYY_PATTERN = DateTimeFormatter.ofPattern("ddMMyyyy");
    DateTimeFormatter MMDDYYYY_HHMMSS_PATTERN = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
}