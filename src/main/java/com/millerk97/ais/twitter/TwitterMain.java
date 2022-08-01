package com.millerk97.ais.twitter;

import com.millerk97.ais.twitter.api.TwitterApiClient;
import com.millerk97.ais.twitter.api.TwitterApiClientImpl;
import twitter4j.TwitterException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TwitterMain {

    public static void main(String[] args) throws TwitterException, IOException, ParseException {
        // TwitterProxy twitterProxy = new TwitterProxy();
        // twitterProxy.fetchTweets();
        System.setProperty("user.timezone", "GMT");

        TwitterApiClient twitter = new TwitterApiClientImpl();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Long start = formatter.parse("16.04.2021").getTime();
        Long end = formatter.parse("17.04.2021").getTime();


        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        isoFormatter.setTimeZone(tz);

        String startTime = isoFormatter.format(new Date(start));
        String endTime = isoFormatter.format(new Date(end));

        System.out.println(twitter.searchTweets("Dogecoin OR Doge", startTime, endTime));
        //System.out.println(twitter.searchTweets("Dogecoin OR Doge"));
    }

}