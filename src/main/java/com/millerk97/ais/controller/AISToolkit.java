package com.millerk97.ais.controller;

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
import com.millerk97.ais.util.Formatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;

import java.util.*;

public class AISToolkit {

    private static String CRYPTOCURRENCY, QUERY, TICKER;
    private static double BREAKOUT_THRESHOLD, PCC, TWITTER_INFLUENCE_FACTOR;
    private final StringProperty messageProperty = new SimpleStringProperty();
    private final int DURATION_OF_HOUR_IN_SECONDS = 3600;
    private boolean PRINT_ANOMALIES;
    private Long START, END;
    private int WINDOW_SIZE;

    public static double calculateCandleVelocity(OHLC ohlc) {
        return (ohlc.getHigh() - ohlc.getLow()) * ohlc.getVolumeTo() / 10000;
    }

    public static double getAssociatedBitcoinMagnitude(Long timestamp) {
        Dataframe df = DataframeUtil.getDataframe(timestamp, "bitcoin");
        if (df == null) return 0;
        return calculateCandleVelocity(df.getOhlc()) / df.getStatistics().getMeanFluctuation();
    }

    public static double calculateOutbreakMagnitude(Pair<OHLC, OHLCStatistics> statisticsPair) {
        return calculateCandleVelocity(statisticsPair.getKey()) / statisticsPair.getValue().getMeanFluctuation();
    }

    public static double calculateOutbreakMagnitudeAttributableToExternalFactors(Pair<OHLC, OHLCStatistics> statisticsPair) {
        double bitcoinMagnitude = 0;
        if (!(CRYPTOCURRENCY.equals("Bitcoin") || CRYPTOCURRENCY.equals("Bitcoin"))) {
            bitcoinMagnitude = getAssociatedBitcoinMagnitude(statisticsPair.getKey().getTime());
        }
        return calculateCandleVelocity(statisticsPair.getKey()) / statisticsPair.getValue().getMeanFluctuation() - bitcoinMagnitude * PCC;
    }

    public void fetchOHLC(boolean reloadOHLC) {
        messageProperty.set("Fetching OHLC data");
        DataFetcher.fetchOHLC(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue(), reloadOHLC);
        messageProperty.set("OHLC data fetched");
    }

    public List<DFTweet> getTweetsFromDailyTimeframe(Long timestamp) {
        List<Dataframe> dfs = DataframeUtil.getDataframesForDay(timestamp, CRYPTOCURRENCY);
        List<DFTweet> tweets = new ArrayList<>();
        double engagementSum = 0;
        for (Dataframe df : dfs) {
            for (DFTweet t : df.getTweets()) {
                engagementSum += t.calculateEngagement();
                tweets.add(t);
            }
        }
        double finalEngagementSum = engagementSum;
        tweets.stream().forEach(t -> t.setEngagementShare(t.calculateEngagement() / finalEngagementSum));
        return tweets;
    }

    public List<DFTweet> getTweetsFromHourlyTimeframe(Long timestamp) {
        Dataframe df = DataframeUtil.getDataframe(timestamp, CRYPTOCURRENCY);
        List<DFTweet> tweets = new ArrayList<>();
        double engagementSum = 0;
        for (DFTweet t : df.getTweets()) {
            engagementSum += t.calculateEngagement();
        }
        for (DFTweet t : df.getTweets()) {
            t.setEngagementShare(t.calculateEngagement() / engagementSum);
            tweets.add(t);
        }
        return tweets;
    }

    public List<TweetMap> mapTweetsToUser(boolean onlyUseOriginalTweets) {
        messageProperty.set("Mapping Tweets");
        long currentTimestamp = START + DURATION_OF_HOUR_IN_SECONDS;
        List<OHLC> anomalies = findPriceActionAnomalies();
        Map<String, TweetMap> map = new HashMap<>();

        while (currentTimestamp + DURATION_OF_HOUR_IN_SECONDS <= END) {
            messageProperty.set("Mapping date: " + Formatter.prettyFormat(currentTimestamp * 1000));
            Dataframe df = DataframeUtil.getDataframe(currentTimestamp, CRYPTOCURRENCY);
            if (df == null) {
                /*
                FlowController.log("Dataframe for Timestamp " + currentTimestamp + "(" + TimeFormatter.formatISO8601(currentTimestamp * 1000) + ") is null");
                 */
                currentTimestamp += DURATION_OF_HOUR_IN_SECONDS;
                continue;
            }
            double engagementSum = 0;
            for (DFTweet t : df.getTweets()) {
                engagementSum += !onlyUseOriginalTweets || t.isOriginal() ? t.calculateEngagement() : 0;
            }
            for (DFTweet t : df.getTweets()) {
                String key = t.getUser().getUsername();
                if (map.get(key) == null) {
                    map.put(key, new TweetMap(t.getUser(), BREAKOUT_THRESHOLD, TWITTER_INFLUENCE_FACTOR));
                }
                TweetMap tm = map.get(key);
                if (t.isOriginal() || !onlyUseOriginalTweets) {
                    if (anomalies.contains(df.getOhlc())) {
                        tm.incrementAnomalyTweetCount();
                    } else tm.incrementRegularTweetCount();
                    tm.addToMagnitude(calculateOutbreakMagnitude(new Pair<>(df.getOhlc(), df.getStatistics())));
                    t.setEngagementShare(t.calculateEngagement() / engagementSum);
                    tm.getTweets().add(t);
                }
            }
            currentTimestamp += DURATION_OF_HOUR_IN_SECONDS;
        }
        messageProperty.set("Mapping Tweets Finished");
        return new ArrayList<>(map.keySet().stream().map(map::get).toList());
    }

    public void deleteDataframes() {
        messageProperty.set("Deleting existing Dataframes");
        DataframeUtil.deleteDataframes(CRYPTOCURRENCY);
        messageProperty.set("Dataframes deleted");
    }

    public void createDataframes() {
        messageProperty.set("Creating Dataframes");
        List<Pair<OHLC, OHLCStatistics>> statistics = new SlidingWindow(DataFetcher.getHourlyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue()), WINDOW_SIZE).getStatistics();
        statistics.sort(Comparator.comparingInt(o -> (int) o.getKey().getTime()));

        for (Pair<OHLC, OHLCStatistics> pair : statistics) {
            OHLC ohlc = pair.getKey();
            OHLCStatistics stats = pair.getValue();
            messageProperty.set("Creating Dataframe for " + Formatter.prettyFormat(ohlc.getTime() * 1000));
            DataframeUtil.storeDataframe(CRYPTOCURRENCY, ohlc, stats, TweetFetcher.fetchTweets(CRYPTOCURRENCY, QUERY, Formatter.formatISO8601(ohlc.getTime() * 1000), Formatter.formatISO8601((ohlc.getTime() + DURATION_OF_HOUR_IN_SECONDS) * 1000)));
        }
        messageProperty.set("Dataframes created");
    }

    public void fetchAllTweetsFromStartToEnd() {
        Long currentTimestamp = START;
        messageProperty.set("Fetching Tweets from API");
        while (currentTimestamp + DURATION_OF_HOUR_IN_SECONDS <= END) {
            List<Tweet> tweets = null;
            while (tweets == null) {
                try {
                    // throws ApiException if too many tweets were fetched in a short period of time, repeat fetching for each timeframe so it doesn't skip anything
                    tweets = TweetFetcher.fetchTweets(CRYPTOCURRENCY, QUERY, Formatter.formatISO8601(currentTimestamp * 1000), Formatter.formatISO8601((currentTimestamp + DURATION_OF_HOUR_IN_SECONDS) * 1000));
                } catch (TwitterApiException e) {
                    try {
                        FlowController.log("API limit reached, waiting 2 minutes for retry");
                        Thread.sleep(120000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            messageProperty.set("Fetched Tweets for Timeframe " + Formatter.prettyFormat(currentTimestamp * 1000));
            currentTimestamp += DURATION_OF_HOUR_IN_SECONDS;
        }
        messageProperty.set("Finished Fetching Tweets from API");
    }

    private List<OHLC> findPriceActionAnomalies() {
        System.setProperty("user.timezone", "UTC");
        List<OHLC> anomalies = new ArrayList<>();
        // a list of OHLC and the mean fluctuation/SD at the respective OHLC for given window size
        SlidingWindow slidingWindow = new SlidingWindow(DataFetcher.getHourlyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue()), WINDOW_SIZE);
        List<Pair<OHLC, OHLCStatistics>> statistics = slidingWindow.getStatistics();
        statistics.sort(Comparator.comparingInt(o -> (int) o.getKey().getTime()));

        for (Pair<OHLC, OHLCStatistics> pair : statistics) {
            OHLC ohlc = pair.getKey();
            OHLCStatistics stats = pair.getValue();

            final boolean anomaly = isAnomaly(pair);
            if (anomaly) {
                anomalies.add(ohlc);
            }

            if (PRINT_ANOMALIES) {
                long timestamp = ohlc.getTime();
                System.out.printf("TF: hourly | On: %s (%s) | Threshold: %15.9f | This: %15.9f | %s%n", Formatter.prettyFormat(timestamp * 1000), timestamp, BREAKOUT_THRESHOLD * stats.getMeanFluctuation(), calculateCandleVelocity(ohlc), anomaly ? " XXX" : "");
            }
        }
        return anomalies;
    }

    public List<Pair<OHLC, OHLCStatistics>> getCandlesSortedChronologically(boolean daily) {
        List<OHLC> candles = daily ? DataFetcher.getDailyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue()) : DataFetcher.getHourlyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue());

        SlidingWindow slidingWindow = new SlidingWindow(candles, WINDOW_SIZE);
        List<Pair<OHLC, OHLCStatistics>> statistics = slidingWindow.getStatistics();
        statistics.sort(Comparator.comparingDouble(o -> -o.getKey().getTime()));
        return statistics;
    }

    public List<Pair<OHLC, OHLCStatistics>> getCandlesSortedByOutbreakMagnitude(boolean daily) {
        List<OHLC> candles = daily ? DataFetcher.getDailyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue()) : DataFetcher.getHourlyOHLCForTimeframe(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue());

        SlidingWindow slidingWindow = new SlidingWindow(candles, WINDOW_SIZE);
        List<Pair<OHLC, OHLCStatistics>> statistics = slidingWindow.getStatistics();
        statistics.sort(Comparator.comparingDouble(o -> -calculateOutbreakMagnitude(o)));

        for (Pair<OHLC, OHLCStatistics> o : statistics) {
            if (PRINT_ANOMALIES) {
                FlowController.log(String.format("TF: hourly | On: %s (%s) | Threshold: %15.9f | This: %15.9f | Magnitude: %s", Formatter.prettyFormat(o.getKey().getTime() * 1000), o.getKey().getTime(), BREAKOUT_THRESHOLD * o.getValue().getMeanFluctuation(), calculateCandleVelocity(o.getKey()), calculateOutbreakMagnitude(o)));
            }
        }

        return statistics;
    }

    public boolean isAnomaly(Pair<OHLC, OHLCStatistics> stats) {
        return calculateCandleVelocity(stats.getKey()) > BREAKOUT_THRESHOLD * stats.getValue().getMeanFluctuation();
    }

    public void setCryptocurrency(String cryptocurrency) {
        CRYPTOCURRENCY = cryptocurrency;
    }

    public void setPCC(double pcc) {
        PCC = pcc;
    }

    public void setQuery(String query) {
        QUERY = query;
    }

    public void setTicker(String ticker) {
        TICKER = ticker;
    }

    public void setStart(Long start) {
        this.START = start;
    }

    public void setEnd(Long end) {
        this.END = end;
    }

    public void setBreakoutThreshold(double breakoutThreshold) {
        BREAKOUT_THRESHOLD = breakoutThreshold;
    }

    public void setWindowSize(int windowSize) {
        WINDOW_SIZE = windowSize;
    }

    public void setPrintAnomalies(boolean printAnomalies) {
        PRINT_ANOMALIES = printAnomalies;
    }

    public StringProperty getMessageProperty() {
        return messageProperty;
    }

    public void setTwitterInfluenceFactor(double twitterInfluenceFactor) {
        TWITTER_INFLUENCE_FACTOR = twitterInfluenceFactor;
    }
}
