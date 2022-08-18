package com.millerk97.ais;

import com.millerk97.ais.cryptocompare.calc.SlidingWindow;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLCStatistics;
import com.millerk97.ais.cryptocompare.impl.DataFetcher;
import com.millerk97.ais.dataframe.DataframeUtil;
import com.millerk97.ais.dataframe.model.DFTweet;
import com.millerk97.ais.dataframe.model.Dataframe;
import com.millerk97.ais.dataframe.model.TweetMap;
import com.millerk97.ais.twitter.TweetFetcher;
import com.millerk97.ais.twitter.api.TwitterApiException;
import com.millerk97.ais.twitter.data.Tweet;
import com.millerk97.ais.util.PropertiesLoader;
import com.millerk97.ais.util.TimeFormatter;
import javafx.util.Pair;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AIS {

    static final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");
    private static final String ELON_MUSK_ID = "44196397";
    private static final int DURATION_OF_HOUR_IN_SECONDS = 3600;
    private static final boolean FETCH_TWEETS = false;
    private static final boolean CREATE_DATAFRAMES = true;
    private static final boolean DELETE_EXISTING_DATAFRAMES = true;
    private static final boolean PRINT_ANOMALIES = true;
    private static final boolean MAP_TWEETS = true;
    private static final boolean RELOAD_OHLC = false;

    private static String CRYPTOCURRENCY, QUERY, TICKER;
    private static Long START, END;
    private static double BREAKOUT_THRESHOLD;
    private static int WINDOW_SIZE;

    public static void main(String[] args) {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "UTC");
        // ISCalculator.calculateInfluencabilityScore("bitcoin");
        run();
    }

    public static void run() {
        try {
            CRYPTOCURRENCY = PropertiesLoader.loadProperty("cryptocurrency");
            QUERY = PropertiesLoader.loadProperty("query");
            TICKER = PropertiesLoader.loadProperty("ticker");
            START = timestampCreator.parse(PropertiesLoader.loadProperty("start")).getTime() / 1000;
            END = timestampCreator.parse(PropertiesLoader.loadProperty("end")).getTime() / 1000;
            WINDOW_SIZE = Integer.parseInt(PropertiesLoader.loadProperty("windowSize"));
            BREAKOUT_THRESHOLD = Double.parseDouble(PropertiesLoader.loadProperty("breakoutThreshold"));

            DataFetcher.fetchOHLC(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue(), RELOAD_OHLC);
            // DAYS
            // printMostSignificantCandles(DataFetcher.getDailyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue()));
            // HOURS
            printMostSignificantCandles(DataFetcher.getHourlyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue()));

            if (true) return;

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

            if (MAP_TWEETS) {
                System.out.println("Mapping Tweets");
                mapTweets();
            }

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void mapTweets() {
        Map<String, Map<String, Long>> mappedTweets = new HashMap<>();
        long currentTimestamp = START + DURATION_OF_HOUR_IN_SECONDS;
        List<OHLC> anomalies = findPriceActionAnomalies();

        while (currentTimestamp + DURATION_OF_HOUR_IN_SECONDS <= END) {
            Dataframe df = DataframeUtil.getDataframe(currentTimestamp, CRYPTOCURRENCY);
            if (df == null) {
                System.out.println("Dataframe for Timestamp " + currentTimestamp + "(" + TimeFormatter.formatISO8601(currentTimestamp * 1000) + ") is null");
                currentTimestamp += DURATION_OF_HOUR_IN_SECONDS;
                continue;
            }
            for (DFTweet t : df.getTweets()) {
                String key = t.getUser().getUsername();
                if (mappedTweets.get(key) == null) {
                    // initialize a new HashMap for the respective user
                    Map<String, Long> userTweets = new HashMap<>();
                    userTweets.put("anomaly", 0L);
                    userTweets.put("regular", 0L);
                    mappedTweets.put(key, userTweets);
                }
                String subKey = anomalies.contains(df.getOhlc()) ? "anomaly" : "regular";
                mappedTweets.get(key).put(subKey, mappedTweets.get(key).get(subKey) + 1);
            }
            currentTimestamp += DURATION_OF_HOUR_IN_SECONDS;
        }

        // transform into data class here as HashMap is more performant but not sortable
        List<TweetMap> tweetMapList = new ArrayList<>(mappedTweets.keySet().stream().map(authorId -> new TweetMap(authorId, mappedTweets.get(authorId).get("anomaly"), mappedTweets.get(authorId).get("regular"))).sorted(Comparator.comparing(TweetMap::computeAnomalyRatio)).toList());

        TweetMap elon = null;
        for (TweetMap user : tweetMapList) {
            if (user.getAnomalyTweetCount() > 0 && user.getTotalTweetCount() > 5) {
                System.out.println("Author: " + user.getAuthorId());
                System.out.println("   regular: " + user.getRegularTweetCount());
                System.out.println("   anomaly: " + user.getAnomalyTweetCount());
                System.out.println("   ratio  : " + user.computeAnomalyRatio());
            }
            if (user.getAuthorId().equals("elonmusk"))
                elon = user;
        }

        if (elon != null) {
            System.out.println("Author: " + elon.getAuthorId());
            System.out.println("   regular: " + elon.getRegularTweetCount());
            System.out.println("   anomaly: " + elon.getAnomalyTweetCount());
            System.out.println("   ratio  : " + elon.computeAnomalyRatio());
        }
    }

    private static List<OHLC> findPriceActionAnomalies() {
        System.setProperty("user.timezone", "UTC");
        List<OHLC> anomalies = new ArrayList<>();
        // a list of OHLC and the mean fluctuation/SD at the respective OHLC for given window size
        SlidingWindow slidingWindow = new SlidingWindow(DataFetcher.getHourlyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue()), WINDOW_SIZE);
        List<Pair<OHLC, OHLCStatistics>> statistics = slidingWindow.getStatistics();
        statistics.sort(Comparator.comparingInt(o -> (int) o.getKey().getTime()));

        for (Pair<OHLC, OHLCStatistics> pair : statistics) {
            OHLC ohlc = pair.getKey();
            OHLCStatistics stats = pair.getValue();

            final boolean anomaly = SlidingWindow.calculateOHLCImpact(ohlc) > BREAKOUT_THRESHOLD * stats.getMeanFluctuation();
            if (anomaly) {
                anomalies.add(ohlc);
            }

            if (PRINT_ANOMALIES) {
                long timestamp = ohlc.getTime();
                System.out.printf("TF: hourly | On: %s (%s) | Threshold: %15.9f | This: %15.9f | %s%n", TimeFormatter.prettyFormat(timestamp * 1000), timestamp, BREAKOUT_THRESHOLD * stats.getMeanFluctuation(), SlidingWindow.calculateOHLCImpact(ohlc), anomaly ? " XXX" : "");
            }
        }
        return anomalies;
    }

    private static void createDataframes() {
        if (DELETE_EXISTING_DATAFRAMES) {
            DataframeUtil.deleteDataframes(CRYPTOCURRENCY);
        }

        List<Pair<OHLC, OHLCStatistics>> statistics = new SlidingWindow(DataFetcher.getHourlyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue()), WINDOW_SIZE).getStatistics();
        statistics.sort(Comparator.comparingInt(o -> (int) o.getKey().getTime()));

        for (Pair<OHLC, OHLCStatistics> pair : statistics) {
            OHLC ohlc = pair.getKey();
            OHLCStatistics stats = pair.getValue();
            DataframeUtil.storeDataframe(CRYPTOCURRENCY, ohlc, stats, TweetFetcher.fetchTweets(CRYPTOCURRENCY, QUERY, TimeFormatter.formatISO8601(ohlc.getTime() * 1000), TimeFormatter.formatISO8601((ohlc.getTime() + DURATION_OF_HOUR_IN_SECONDS) * 1000)));
        }
    }

    private static void fetchAllTweetsInTimeframe() {
        Long currentTimestamp = START;
        while (currentTimestamp + DURATION_OF_HOUR_IN_SECONDS <= END) {
            List<Tweet> tweets = null;
            while (tweets == null) {
                try {
                    // throws ApiException if too many tweets were fetched in a short period of time, repeat fetching for each timeframe so it doesn't skip anything
                    tweets = TweetFetcher.fetchTweets(CRYPTOCURRENCY, QUERY, TimeFormatter.formatISO8601(currentTimestamp * 1000), TimeFormatter.formatISO8601((currentTimestamp + DURATION_OF_HOUR_IN_SECONDS) * 1000));
                } catch (TwitterApiException e) {
                    try {
                        System.out.println("API limit reached, waiting 2 minutes for retry");
                        Thread.sleep(120000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            System.out.println("done");
            currentTimestamp += DURATION_OF_HOUR_IN_SECONDS;
        }
    }

    private static void printMostSignificantCandles(List<OHLC> candles) {
        SlidingWindow slidingWindow = new SlidingWindow(candles, WINDOW_SIZE);
        List<Pair<OHLC, OHLCStatistics>> statistics = slidingWindow.getStatistics();
        statistics.sort(Comparator.comparingDouble(o -> SlidingWindow.calculateOHLCImpact(o.getKey()) / o.getValue().getMeanFluctuation()));

        for (Pair<OHLC, OHLCStatistics> o : statistics) {
            System.out.printf("TF: hourly | On: %s (%s) | Threshold: %15.9f | This: %15.9f | Magnitude: %s%n", TimeFormatter.prettyFormat(o.getKey().getTime() * 1000), o.getKey().getTime(), BREAKOUT_THRESHOLD * o.getValue().getMeanFluctuation(), SlidingWindow.calculateOHLCImpact(o.getKey()), SlidingWindow.calculateOHLCImpact(o.getKey()) / o.getValue().getMeanFluctuation());
        }
    }
}
