package me.rhys.backup.util.time;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    public static String getSystemTime() {
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }
}
