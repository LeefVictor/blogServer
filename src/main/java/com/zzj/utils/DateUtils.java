package com.zzj.utils;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtils {

    public static String transToYMD(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.getDayOfMonth() + "," + getShortMonth(localDateTime) + " " + localDateTime.getYear();
    }

    public static String getShortMonth(LocalDateTime localDateTime) {
        return localDateTime.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }
}
