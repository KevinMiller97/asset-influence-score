package com.millerk97.ais.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Formatter {

    public static String formatISO8601(Long timestamp) {
        DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormatter.format(new Date(timestamp));
    }

    public static String prettyFormatShort(Long timestamp) {
        DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormatter.format(new Date(timestamp)).substring(0, 11).replace("T", "");
    }

    public static String prettyFormat(Long timestamp) {
        DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormatter.format(new Date(timestamp)).substring(0, 16).replace("T", " ");
    }

    public static String prettyFormatRange(Long start, Long end) {
        DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormatter.format(new Date(start)).substring(0, 16).replace("T", " ") + " - " + isoFormatter.format(new Date(end)).substring(11, 16);
    }

    public static String formatNumberDecimal(Double number) {
        return String.format(Locale.US, "%,.4f", number);
    }

    public static String formatNumber(Double number) {
        return String.format(Locale.US, "%,d", number);
    }

    public static String formatNumber(int number) {
        return String.format(Locale.US, "%,d", number);
    }

}
