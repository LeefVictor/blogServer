package com.zzj.utils;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtils {

    public static String transToYMD(LocalDateTime localDateTime){
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.getDayOfMonth() + "," + (localDateTime.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault())) + " " + localDateTime.getYear();
    }
}
