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
import com.millerk97.ais.util.TimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;

import java.util.*;

public class AISToolkit {

    private final StringProperty messageProperty = new SimpleStringProperty();
    private final int DURATION_OF_HOUR_IN_SECONDS = 3600;
    private boolean PRINT_ANOMALIES;
    private String CRYPTOCURRENCY, QUERY, TICKER;
    private Long START, END;
    private double BREAKOUT_THRESHOLD;
    private int WINDOW_SIZE;

    public static double calculateOutbreakMagnitude(Pair<OHLC, OHLCStatistics> statisticsPair) {
        return SlidingWindow.calculateOHLCImpact(statisticsPair.getKey()) / statisticsPair.getValue().getMeanFluctuation();
    }

    public void fetchOHLC(boolean reloadOHLC) {
        messageProperty.set("Fetching OHLC data");
        DataFetcher.fetchOHLC(CRYPTOCURRENCY, TICKER, START.intValue(), END.intValue(), reloadOHLC);
        messageProperty.set("OHLC data fetched");
    }

    public List<TweetMap> mapTweets() {
        messageProperty.set("Mapping Tweets");
        long currentTimestamp = START + DURATION_OF_HOUR_IN_SECONDS;
        List<OHLC> anomalies = findPriceActionAnomalies();
        Map<String, TweetMap> map = new HashMap<>();

        while (currentTimestamp + DURATION_OF_HOUR_IN_SECONDS <= END) {
            Dataframe df = DataframeUtil.getDataframe(currentTimestamp, CRYPTOCURRENCY);
            if (df == null) {
                /*
                FlowController.log("Dataframe for Timestamp " + currentTimestamp + "(" + TimeFormatter.formatISO8601(currentTimestamp * 1000) + ") is null");
                 */
                currentTimestamp += DURATION_OF_HOUR_IN_SECONDS;
                continue;
            }
            for (DFTweet t : df.getTweets()) {
                String key = t.getUser().getUsername();
                if (map.get(key) == null) {
                    map.put(key, new TweetMap(t.getUser()));
                }
                TweetMap tm = map.get(key);
                if (anomalies.contains(df.getOhlc()))
                    tm.incrementAnomalyTweetCount();
                else tm.incrementRegularTweetCount();
                tm.addToMagnitude(calculateOutbreakMagnitude(new Pair<>(df.getOhlc(), df.getStatistics())));
                tm.getTweets().add(t);
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
            messageProperty.set("Creating Dataframe for " + TimeFormatter.prettyFormat(ohlc.getTime() * 1000));
            DataframeUtil.storeDataframe(CRYPTOCURRENCY, ohlc, stats, TweetFetcher.fetchTweets(CRYPTOCURRENCY, QUERY, TimeFormatter.formatISO8601(ohlc.getTime() * 1000), TimeFormatter.formatISO8601((ohlc.getTime() + DURATION_OF_HOUR_IN_SECONDS) * 1000)));
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
                    tweets = TweetFetcher.fetchTweets(CRYPTOCURRENCY, QUERY, TimeFormatter.formatISO8601(currentTimestamp * 1000), TimeFormatter.formatISO8601((currentTimestamp + DURATION_OF_HOUR_IN_SECONDS) * 1000));
                } catch (TwitterApiException e) {
                    try {
                        FlowController.log("API limit reached, waiting 2 minutes for retry");
                        Thread.sleep(120000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            messageProperty.set("Fetched Tweets for Timeframe " + TimeFormatter.prettyFormat(currentTimestamp));
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
                System.out.printf("TF: hourly | On: %s (%s) | Threshold: %15.9f | This: %15.9f | %s%n", TimeFormatter.prettyFormat(timestamp * 1000), timestamp, BREAKOUT_THRESHOLD * stats.getMeanFluctuation(), SlidingWindow.calculateOHLCImpact(ohlc), anomaly ? " XXX" : "");
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
                FlowController.log(String.format("TF: hourly | On: %s (%s) | Threshold: %15.9f | This: %15.9f | Magnitude: %s", TimeFormatter.prettyFormat(o.getKey().getTime() * 1000), o.getKey().getTime(), BREAKOUT_THRESHOLD * o.getValue().getMeanFluctuation(), SlidingWindow.calculateOHLCImpact(o.getKey()), calculateOutbreakMagnitude(o)));
            }
        }

        return statistics;
    }

    public boolean isAnomaly(Pair<OHLC, OHLCStatistics> stats) {
        return SlidingWindow.calculateOHLCImpact(stats.getKey()) > BREAKOUT_THRESHOLD * stats.getValue().getMeanFluctuation();
    }

    public void setCryptocurrency(String cryptocurrency) {
        this.CRYPTOCURRENCY = cryptocurrency;
    }

    public void setQuery(String query) {
        this.QUERY = query;
    }

    public void setTicker(String ticker) {
        this.TICKER = ticker;
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
}
