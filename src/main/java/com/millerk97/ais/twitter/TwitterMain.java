package com.millerk97.ais.twitter;

import twitter4j.TwitterException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TwitterMain {

    public static void main(String[] args) throws TwitterException, IOException, ParseException {
        System.setProperty("user.timezone", "UTC");
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Long start = formatter.parse("16.04.2021").getTime();
        Long end = formatter.parse("17.04.2021").getTime();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        isoFormatter.setTimeZone(tz);

        String startTime = isoFormatter.format(new Date(start));
        String endTime = isoFormatter.format(new Date(end));

        System.out.println(TweetFetcher.fetchTweets("Dogecoin OR Doge", startTime, endTime));
    }

}