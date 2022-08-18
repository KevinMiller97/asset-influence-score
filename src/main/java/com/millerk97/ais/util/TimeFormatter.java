package com.millerk97.ais.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeFormatter {

    public static String formatISO8601(Long timestamp) {
        DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormatter.format(new Date(timestamp));
    }

    public static String prettyFormat(Long timestamp) {
        DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormatter.format(new Date(timestamp)).substring(0, 16).replace("T", " ");
    }

}
