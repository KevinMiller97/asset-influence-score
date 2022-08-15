package com.millerk97.ais;

import com.millerk97.ais.coingecko.impl.ISCalculator;
import com.millerk97.ais.cryptocompare.calc.AnomalyDay;
import com.millerk97.ais.cryptocompare.calc.SlidingWindow;
import com.millerk97.ais.cryptocompare.constant.Timeframe;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.impl.DataFetcher;
import com.millerk97.ais.dataframe.DataframeUtil;
import com.millerk97.ais.twitter.TweetFetcher;
import com.millerk97.ais.twitter.data.Tweet;
import com.millerk97.ais.util.PropertiesLoader;
import com.millerk97.ais.util.TimeFormatter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIS {

    static final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");
    private static final String ELON_MUSK_ID = "44196397";
    private static final int DURATION_OF_HOUR_IN_SECONDS = 3600;
    private static final int DURATION_OF_DAY_IN_SECONDS = 86400;
    private static final boolean FETCH_TWEETS = false;
    private static final boolean CREATE_DATAFRAMES = true;
    private static final boolean PRINT_ANOMALIES = true;
    private static final boolean returnpoint = true; // TODO REMOVE

    private static String CRYPTOCURRENCY, QUERY, TICKER;
    private static Long START, END;


    public static void main(String[] args) {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "UTC");
        ISCalculator.calculateInfluencabilityScore("bitcoin");
        run();
    }

    public static void run() {
        try {
            CRYPTOCURRENCY = PropertiesLoader.loadProperty("cryptocurrency");
            QUERY = PropertiesLoader.loadProperty("query");
            TICKER = PropertiesLoader.loadProperty("ticker");
            START = timestampCreator.parse(PropertiesLoader.loadProperty("start")).getTime() / 1000;
            END = timestampCreator.parse(PropertiesLoader.loadProperty("end")).getTime() / 1000;

            findAnomalies();

            // this takes roughly 15 hours to complete
            if (FETCH_TWEETS) {
                System.out.println("Fetching all tweets for " + CRYPTOCURRENCY);
                fetchAllTweetsInTimeframe();
                System.out.println("Tweets fetched!");
            }

            if (CREATE_DATAFRAMES) {
                System.out.println("Creating dataframes for " + CRYPTOCURRENCY);
                createDataframes();
                System.out.println("Dataframes created!");
            }

            if (returnpoint)
                return;
            Map<String, Long> userTweets = new HashMap<>();

            for (String authorId : userTweets.keySet()) {
                if (userTweets.get(authorId) > 3)
                    System.out.println("AuthorID: " + authorId + "; No. Tweets: " + userTweets.get(authorId));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fetchAllTweetsInTimeframe() {
        Long currentTimestamp = START;
        while (currentTimestamp + DURATION_OF_HOUR_IN_SECONDS <= END) {
            List<Tweet> tweets = TweetFetcher.fetchTweets(CRYPTOCURRENCY, QUERY, TimeFormatter.formatISO8601(currentTimestamp * 1000), TimeFormatter.formatISO8601((currentTimestamp + DURATION_OF_HOUR_IN_SECONDS) * 1000));
            currentTimestamp += DURATION_OF_HOUR_IN_SECONDS;
        }
    }

    private static void findAnomalies() {
        for (OHLC day : findDailyPriceAnomaliesInTimeframe()) {
            for (OHLC hour : findHourlyPriceAnomaliesForDay(day)) {
                // do something
            }
        }
    }

    private static List<OHLC> findDailyPriceAnomaliesInTimeframe() {
        // window size 14; threshold 2 works very well | also fetches OHLC data if not present
        SlidingWindow dailySlidingWindow = new SlidingWindow(DataFetcher.fetchOHLC(CRYPTOCURRENCY, TICKER, END.intValue()), 14, 2, Timeframe.DAYS_1);
        return dailySlidingWindow.findAnomalies(START, PRINT_ANOMALIES);
    }

    private static List<OHLC> findHourlyPriceAnomaliesForDay(OHLC day) {
        return new AnomalyDay(DataFetcher.getHourlyOHLCForDay(CRYPTOCURRENCY, day), 1.9).findAnomalies(PRINT_ANOMALIES);
    }

    private static void createDataframes() {
        List<OHLC> days = DataFetcher.getDailyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START, END);
        for (OHLC day : days) {
            List<OHLC> hours = DataFetcher.getHourlyOHLCForDay(CRYPTOCURRENCY, day);
            for (OHLC hour : hours) {
                DataframeUtil.storeDataframe(CRYPTOCURRENCY, hour, TweetFetcher.fetchTweets(CRYPTOCURRENCY, QUERY, TimeFormatter.formatISO8601(hour.getTime() * 1000), TimeFormatter.formatISO8601((hour.getTime() + DURATION_OF_HOUR_IN_SECONDS) * 1000)));
            }
        }
    }

    /*
     List<Tweet> tweets = TweetFetcher.fetchTweets(cryptocurrency, query, TimeFormatter.formatISO8601(ohlc.getTime() * 1000), TimeFormatter.formatISO8601((ohlc.getTime() + 3600) * 1000));
                for (Tweet t : tweets) {
                    userTweets.put(t.getUser().getUsername(), userTweets.get(t.getUser().getUsername()) != null ? userTweets.get(t.getUser().getUsername()) + 1 : 1);
                }
                DataframeUtil.storeDataframe(cryptocurrency, ohlc, tweets);
     */
}
