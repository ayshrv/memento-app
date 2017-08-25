package com.example.gaurav.smar_test2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by gaurav on 4/2/17.
 */
public class LogViewer {
    private static List<String> log_list = new ArrayList<>();

    public static void addLog(String new_log) {
        log_list.add(getLogHeader() + new_log);
    }

    public static List<String> getLog() {
        return log_list;
    }
    // Get the current time and add to log.
    private static String getLogHeader() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return "[" + dateFormat.format(Calendar.getInstance().getTime()) + "] ";
    }

}
