package com.mgatelabs.piper.shared.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by @mgatelabs (Michael Fuller) on 3/20/2018.
 */
public class TimeTo {

    public static int MINUTES = 60;
    public static int HOURS = 60 * 60;

    public static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    public static String format(double seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 1,1,0,0,0);
        calendar.add(Calendar.SECOND, (int) seconds);
        return SDF.format(calendar.getTime());
    }
}
