package com.millerk97.ais;

import com.millerk97.ais.cryptocompare.calc.AnomalyDay;
import com.millerk97.ais.cryptocompare.calc.SlidingWindow;
import com.millerk97.ais.cryptocompare.constant.Timeframe;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.impl.DataFetcher;
import com.millerk97.ais.twitter.DataframeUtil;
import com.millerk97.ais.twitter.TweetFetcher;
import com.millerk97.ais.twitter.data.Tweet;
import com.millerk97.ais.util.TimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AIS {

    static final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");

    public static void main(String[] args) {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "UTC");
        bitcoin();
    }

    public static void bitcoin() {
        final String cryptocurrency = "bitcoin";
        final String query = "Bitcoin OR BTC";
        final String ticker = "BTC";
        try {
            Long start = timestampCreator.parse("01.01.2020").getTime() / 1000;
            Long end = timestampCreator.parse("10.07.2022").getTime() / 1000;
            // window size 14; threshold 2 works very well
            SlidingWindow window = new SlidingWindow(DataFetcher.fetchDailyOHLC(cryptocurrency, ticker, end.intValue()), 14, 2, Timeframe.DAYS_1);

            List<OHLC> anomalyDaysOHLC = window.findAnomalies(start, false);

            List<AnomalyDay> anomalyDays = new ArrayList<>();
            anomalyDaysOHLC.stream().map(ohlc -> anomalyDays.add(new AnomalyDay(DataFetcher.getHourlyOHLCsForDay(cryptocurrency, ohlc), 1.9))).collect(Collectors.toList());

            Map<String, Long> userTweets = new HashMap<>();

            for (AnomalyDay day : anomalyDays) {
                List<OHLC> anomalies = day.findAnomalies(false);
                for (OHLC ohlc : anomalies) {
                    List<Tweet> tweets = TweetFetcher.fetchTweets(query, TimeFormatter.formatISO8601(ohlc.getTime() * 1000), TimeFormatter.formatISO8601((ohlc.getTime() + 3600) * 1000));
                    for (Tweet t : tweets) {
                        userTweets.put(t.getUser().getUsername(), userTweets.get(t.getUser().getUsername()) != null ? userTweets.get(t.getUser().getUsername()) + 1 : 1);
                    }
                    DataframeUtil.storeDataframe(cryptocurrency, ohlc, tweets);
                }
            }
            for (String authorId : userTweets.keySet()) {
                if (userTweets.get(authorId) > 3)
                    System.out.println("AuthorID: " + authorId + "; No. Tweets: " + userTweets.get(authorId));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public static void dogecoin() {
        final String ELON_MUSK_ID = "44196397";
        final String cryptocurrency = "dogecoin";
        final String query = "Dogecoin OR Doge";
        final String ticker = "DOGE";

        try {
            Long start = timestampCreator.parse("01.01.2020").getTime() / 1000;
            Long end = timestampCreator.parse("10.07.2022").getTime() / 1000;
            // window size 14; threshold 2 works very well
            SlidingWindow window = new SlidingWindow(DataFetcher.fetchDailyOHLC(cryptocurrency, ticker, end.intValue()), 14, 2, Timeframe.DAYS_1);

            List<OHLC> anomalyDaysOHLC = window.findAnomalies(start, false);

            List<AnomalyDay> anomalyDays = new ArrayList<>();
            anomalyDaysOHLC.stream().map(ohlc -> anomalyDays.add(new AnomalyDay(DataFetcher.getHourlyOHLCsForDay(cryptocurrency, ohlc), 1.9))).collect(Collectors.toList());

            Map<String, Long> userTweets = new HashMap<>();
            List<Tweet> elonTweets = new ArrayList<>();

            for (AnomalyDay day : anomalyDays) {
                List<OHLC> anomalies = day.findAnomalies(true);
                for (OHLC ohlc : anomalies) {
                    List<Tweet> tweets = TweetFetcher.fetchTweets(query, TimeFormatter.formatISO8601(ohlc.getTime() * 1000), TimeFormatter.formatISO8601((ohlc.getTime() + 3600) * 1000));
                    for (Tweet t : tweets) {
                        userTweets.put(t.getUser().getUsername(), userTweets.get(t.getUser().getUsername()) != null ? userTweets.get(t.getUser().getUsername()) + 1 : 1);
                        if (t.getAuthorId().equals(ELON_MUSK_ID)) {
                            elonTweets.add(t);
                        }
                    }
                    DataframeUtil.storeDataframe(cryptocurrency, ohlc, tweets);
                }
            }
            for (String authorId : userTweets.keySet()) {
                if (userTweets.get(authorId) > 3)
                    System.out.println("AuthorID: " + authorId + "; No. Tweets: " + userTweets.get(authorId));
                if (authorId.equals(ELON_MUSK_ID)) {
                    System.err.println("AuthorID: ELON MUSK No. Tweets: " + userTweets.get(authorId));
                }
            }
            for (Tweet t : elonTweets) {
                System.out.println("_____________");
                System.out.println(t.getCreatedAt());
                System.out.println(t.getText());
                System.out.println("_____________");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
