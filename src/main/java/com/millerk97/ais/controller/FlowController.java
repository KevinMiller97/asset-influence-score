package com.millerk97.ais.controller;

import com.millerk97.ais.coingecko.impl.ISCalculator;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLCStatistics;
import com.millerk97.ais.dataframe.DataframeUtil;
import com.millerk97.ais.dataframe.model.DFTweet;
import com.millerk97.ais.dataframe.model.Dataframe;
import com.millerk97.ais.dataframe.model.TweetMap;
import com.millerk97.ais.fxgui.FXBaseApplication;
import com.millerk97.ais.fxgui.components.PriceActionItem;
import com.millerk97.ais.fxgui.components.TweetItem;
import com.millerk97.ais.fxgui.components.UserItem;
import com.millerk97.ais.util.Formatter;
import com.millerk97.ais.util.PropertiesLoader;
import com.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FlowController {

    private static final FXBaseApplication base = new FXBaseApplication();
    private static final Long DURATION_DAY = 86340L; // 23:59
    private static final Long DURATION_HOUR = 3600L;
    private static AISToolkit aisToolkit;
    private static Long start;
    private static Long end;

    private static int breakoutThresholdFactor;
    private static String cryptocurrency;
    private static double pcc;
    private static double twitterInfluenceFactor;
    private static int resultLimit;
    private static int minTweets;
    private static int windowSize;
    private static boolean onlyUseOriginalTweets;
    private static boolean onlyUseVerifiedUsers;


    public static void init(Stage stage) {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "UTC");
        Scene scene = new Scene(base);
        stage.setWidth(1300);
        stage.setHeight(1030);
        stage.setTitle("AIS Calculator");
        stage.setScene(scene);
        base.getStartButton().setOnAction(action -> {
            applyConfiguration();
            run();
        });
        base.getTradeStrategyButton().setOnAction(action -> {
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh7_breakout8_minT10_2day.csv", 7, 8, 10, 172800);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh3_breakout8_minT10_2day.csv", 3, 8, 10, 172800);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh1_breakout8_minT5_2day.csv", 1, 8, 5, 172800);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh1_breakout8_minT10_2day.csv", 1, 8, 10, 172800);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh1_breakout8_minT5_1day.csv", 1, 8, 5, 86400);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh1_breakout8_minT10_1day.csv", 1, 8, 10, 86400);
            applyTradeB("src/main/resources/com/millerk97/trade/refresh1_breakout15_minT5_2day.csv", 1, 15, 5, 172800);
            /*
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh7_breakout8_minT5_2day.csv", 7, 8, 5, 172800);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh3_breakout8_minT5_2day.csv", 3, 8, 5, 172800);

            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh7_breakout8_minT10_2day.csv", 7, 8, 10, 172800);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh3_breakout8_minT10_2day.csv", 3, 8, 10, 172800);

            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh7_breakout8_minT5_1day.csv", 7, 8, 5, 86400);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh3_breakout8_minT5_1day.csv", 3, 8, 5, 86400);

            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh7_breakout8_minT10_1day.csv", 7, 8, 10, 86400);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh3_breakout8_minT10_1day.csv", 3, 8, 10, 86400);

            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh7_breakout15_minT5_2day.csv", 7, 15, 5, 172800);
            applyTradeB("src/main/resources/com/millerk97/trade/B_refresh3_breakout15_minT5_2day.csv", 3, 15, 5, 172800);

            applyTradeA("src/main/resources/com/millerk97/trade/refresh7_breakout8_minT10.csv", 7, 8, 10);
            applyTradeA("src/main/resources/com/millerk97/trade/refresh3_breakout8_minT10.csv", 3, 8, 10);

             */
        });
        stage.show();
    }

    public static void applyTradeB(String FILENAME, int aisRefreshInterval, int breakoutThresholdFactor, int minTweets, int holdDuration) {
        applyConfiguration();
        final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");
        aisToolkit = new AISToolkit();
        aisToolkit.setCryptocurrency(cryptocurrency);
        aisToolkit.setTicker(base.getTickerInput().getText());
        aisToolkit.setQuery(cryptocurrency + " OR " + base.getTickerInput().getText());
        aisToolkit.setPCC(pcc);
        aisToolkit.setWindowSize(windowSize);
        aisToolkit.setBreakoutThreshold(breakoutThresholdFactor);
        aisToolkit.setTwitterInfluenceFactor(twitterInfluenceFactor);
        Long TRADE_START = 0L;
        Long TRADE_END = 0L;
        try {
            start = timestampCreator.parse("01.01.2020").getTime() / 1000;
            end = timestampCreator.parse("01.01.2021").getTime() / 1000;
            TRADE_START = timestampCreator.parse("01.01.2021").getTime() / 1000;
            TRADE_END = timestampCreator.parse("31.12.2021").getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        aisToolkit.setStart(start);
        aisToolkit.setEnd(end);


        double balance = 10000d;
        double currentHoldings = 0d;
        final int limitTopUsers = 8;
        double investorAHoldings = 1000000d;
        double investorBHoldings = 200000d;
        double investorCHoldings = 22222d;
        int counter = 0;

        Long hourlyTimestamp = TRADE_START;
        Long dailyTimestamp = TRADE_START;
        Long latestEntry = 0L;

        List<String[]> tradingDays = new ArrayList<>();

        List<TweetMap> tweetMapList = aisToolkit.mapTweetsToUser(onlyUseOriginalTweets).stream().filter(entry -> entry.getTotalTweetCount() > minTweets).filter(entry -> entry.getUser().isVerified()).sorted(Comparator.comparingDouble(o -> -o.getAIS())).limit(limitTopUsers).collect(Collectors.toList());

        while (dailyTimestamp <= TRADE_END) {
            // current top 5 users tweetmaps by AIS
            // go over the current day
            if (counter % aisRefreshInterval == 0) {
                tweetMapList = aisToolkit.mapTweetsToUser(onlyUseOriginalTweets).stream().filter(entry -> entry.getTotalTweetCount() > minTweets).filter(entry -> entry.getUser().isVerified()).sorted(Comparator.comparingDouble(o -> -o.getAIS())).limit(limitTopUsers).collect(Collectors.toList());
                System.out.println("Current AIS Leaderboard: ");
                for (TweetMap tm : tweetMapList) {
                    System.out.println(String.format("User: %s (%f)", tm.getUser().getUsername(), tm.getAIS()));
                }
            }
            while (hourlyTimestamp + 3600 <= dailyTimestamp + 86400) {
                Dataframe df = DataframeUtil.getDataframe(hourlyTimestamp, cryptocurrency);
                // iterate over top 5 users
                if (df != null) {
                    for (TweetMap tm : tweetMapList) {
                        for (DFTweet t : df.getTweets()) {
                            if (t.getUser().getUsername().equals(tm.getUser().getUsername())) {
                                // this user tweeted on this day
                                double availableBudget = balance * tm.getAIS() / 100; // we could go all in here too, TODO add as option potentially
                                balance -= availableBudget;
                                double entryPrice = (df.getOhlc().getOpen() + (df.getOhlc().getClose() - df.getOhlc().getOpen()) / 5); // 20% premium
                                double entryAmount = availableBudget / entryPrice;
                                currentHoldings += entryAmount;
                                latestEntry = dailyTimestamp;
                                System.out.println(String.format("Buying %f Dogecoin at entry price of %f worth %f because %s tweeted at %s", entryAmount, entryPrice, entryAmount * entryPrice, t.getUser().getUsername(), t.getCreatedAt()));
                            }
                        }
                    }
                }
                hourlyTimestamp += 3600;
            }
            // close open positions
            Dataframe dfClose = DataframeUtil.getDataframe(hourlyTimestamp, cryptocurrency);
            System.out.println("____ Balances on: " + Formatter.prettyFormat(dailyTimestamp * 1000));
            if (dfClose != null) {
                if (currentHoldings > 0) {
                    if (dailyTimestamp >= latestEntry + holdDuration) {
                        double tradeResult = currentHoldings * dfClose.getOhlc().getOpen();
                        balance += tradeResult;
                        currentHoldings = 0;
                        System.out.println(String.format("Selling Dogecoin for %f", tradeResult));
                    } else {
                        System.out.println("Holding Dogecoin until " + Formatter.prettyFormat((dailyTimestamp + 172800) * 1000));
                    }
                } else {
                    System.out.println("Nothing to trade from " + Formatter.prettyFormatRange(dailyTimestamp * 1000, (dailyTimestamp + 86340) * 1000));
                }
                System.out.println("Investor A: " + investorAHoldings * dfClose.getOhlc().getOpen());
                System.out.println("Investor B: " + investorBHoldings * dfClose.getOhlc().getOpen());
                System.out.println("Trader: " + (balance + currentHoldings * dfClose.getOhlc().getOpen()));
                System.out.println("_________________");
                tradingDays.add(new String[]{Formatter.prettyFormat(hourlyTimestamp * 1000), round(balance + currentHoldings * dfClose.getOhlc().getOpen(), 2)});
            } else {
                System.out.println("dataframe is null");
            }
            dailyTimestamp += 86400;
            aisToolkit.setEnd(dailyTimestamp);
            counter++;
        }
        try {
            FileWriter outputfile = new FileWriter(FILENAME);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeAll(tradingDays);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void applyTradeA(String FILENAME, int aisRefreshInterval, int breakoutThresholdFactor, int minTweets) {
        applyConfiguration();
        final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");
        aisToolkit = new AISToolkit();
        aisToolkit.setCryptocurrency(cryptocurrency);
        aisToolkit.setTicker(base.getTickerInput().getText());
        aisToolkit.setQuery(cryptocurrency + " OR " + base.getTickerInput().getText());
        aisToolkit.setPCC(pcc);
        aisToolkit.setWindowSize(windowSize);
        aisToolkit.setBreakoutThreshold(breakoutThresholdFactor);
        aisToolkit.setTwitterInfluenceFactor(twitterInfluenceFactor);
        Long TRADE_START = 0L;
        Long TRADE_END = 0L;
        try {
            start = timestampCreator.parse("01.01.2020").getTime() / 1000;
            end = timestampCreator.parse("01.01.2021").getTime() / 1000;
            TRADE_START = timestampCreator.parse("01.01.2021").getTime() / 1000;
            TRADE_END = timestampCreator.parse("31.12.2021").getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        aisToolkit.setStart(start);
        aisToolkit.setEnd(end);


        double balance = 10000d;
        double currentHoldings = 0d;
        final int limitTopUsers = 8;
        double investorAHoldings = 1000000d;
        double investorBHoldings = 200000d;
        double investorCHoldings = 22222d;
        int counter = 0;

        Long hourlyTimestamp = TRADE_START;
        Long dailyTimestamp = TRADE_START;

        List<String[]> tradingDays = new ArrayList<>();

        List<TweetMap> tweetMapList = aisToolkit.mapTweetsToUser(onlyUseOriginalTweets).stream().filter(entry -> entry.getTotalTweetCount() > minTweets).filter(entry -> entry.getUser().isVerified()).sorted(Comparator.comparingDouble(o -> -o.getAIS())).limit(limitTopUsers).collect(Collectors.toList());

        while (dailyTimestamp <= TRADE_END) {
            // current top 5 users tweetmaps by AIS
            // go over the current day
            if (counter % aisRefreshInterval == 0) {
                tweetMapList = aisToolkit.mapTweetsToUser(onlyUseOriginalTweets).stream().filter(entry -> entry.getTotalTweetCount() > minTweets).filter(entry -> entry.getUser().isVerified()).sorted(Comparator.comparingDouble(o -> -o.getAIS())).limit(limitTopUsers).collect(Collectors.toList());
                System.out.println("Current AIS Leaderboard: ");
                for (TweetMap tm : tweetMapList) {
                    System.out.println(String.format("User: %s (%f)", tm.getUser().getUsername(), tm.getAIS()));
                }
            }
            while (hourlyTimestamp + 3600 <= dailyTimestamp + 86400) {
                Dataframe df = DataframeUtil.getDataframe(hourlyTimestamp, cryptocurrency);
                // iterate over top 5 users
                if (df != null) {
                    for (TweetMap tm : tweetMapList) {
                        for (DFTweet t : df.getTweets()) {
                            if (t.getUser().getUsername().equals(tm.getUser().getUsername())) {
                                if (balance < 0) {
                                    System.err.println("buggy code bro");
                                }
                                // this user tweeted on this day
                                double availableBudget = balance * tm.getAIS() / 100; // we could go all in here too, TODO add as option potentially
                                balance -= availableBudget;
                                double entryPrice = (df.getOhlc().getOpen() + (df.getOhlc().getClose() - df.getOhlc().getOpen()) / 5); // 20% premium
                                double entryAmount = availableBudget / entryPrice;
                                currentHoldings += entryAmount;
                                System.out.println(String.format("Buying %f Dogecoin at entry price of %f worth %f because %s tweeted at %s", entryAmount, entryPrice, entryAmount * entryPrice, t.getUser().getUsername(), t.getCreatedAt()));
                            }
                        }
                    }
                }
                hourlyTimestamp += 3600;
            }
            // close open positions
            Dataframe dfClose = DataframeUtil.getDataframe(hourlyTimestamp, cryptocurrency);
            System.out.println("____ Balances on: " + Formatter.prettyFormat(dailyTimestamp * 1000));
            if (dfClose != null) {
                if (currentHoldings > 0) {
                    double tradeResult = currentHoldings * dfClose.getOhlc().getOpen();
                    balance += tradeResult;
                    currentHoldings = 0;
                    System.out.println(String.format("Selling Dogecoin for %f", tradeResult));
                } else {
                    System.out.println("Nothing to trade from " + Formatter.prettyFormatRange(dailyTimestamp * 1000, (dailyTimestamp + 86340) * 1000));
                }
                System.out.println("Investor A: " + investorAHoldings * dfClose.getOhlc().getOpen());
                System.out.println("Investor B: " + investorBHoldings * dfClose.getOhlc().getOpen());
                System.out.println("Investor C: " + investorCHoldings * dfClose.getOhlc().getOpen());
                System.out.println("Trader: " + balance);
                System.out.println("_________________");
                tradingDays.add(new String[]{Formatter.prettyFormat(hourlyTimestamp * 1000), round(investorAHoldings * dfClose.getOhlc().getOpen(), 2), round(investorBHoldings * dfClose.getOhlc().getOpen(), 2), round(investorCHoldings * dfClose.getOhlc().getOpen(), 2), round(balance, 2), round(dfClose.getOhlc().getOpen(), 7)});
            } else {
                System.out.println("dataframe is null");
            }
            dailyTimestamp += 86400;
            aisToolkit.setEnd(dailyTimestamp);
            counter++;
        }
        try {
            FileWriter outputfile = new FileWriter(FILENAME);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeAll(tradingDays);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String round(double input, int places) {
        return String.valueOf(new BigDecimal(input).setScale(places, RoundingMode.HALF_UP).doubleValue());
    }

    public static void run() {
        Platform.runLater(() -> {
            // clear everything in case of multiple executions
            base.getPriceActionHighestDailyVelocityContent().getChildren().clear();
            base.getPriceActionHighestHourlyVelocityContent().getChildren().clear();
            base.getPriceActionChronologicalDailyContent().getChildren().clear();
            base.getPriceActionChronologicalHourlyContent().getChildren().clear();
            base.getUserContent().getChildren().clear();
            base.getSelectedTweetContent().getChildren().clear();
        });

        try {
            initializeAISToolkit();
        } catch (IOException | ParseException e) {
            log(e.getMessage());
            e.printStackTrace();
        }


        aisToolkit.getMessageProperty().addListener((observableValue, oldValue, message) -> Platform.runLater(() -> base.setStatusMessage(message)));

        // Threads prevent the GUI from freezing
        ThreadWithOnFinished exchangeSupportScoreThread = new ThreadWithOnFinished() {
            @Override
            public void doRun() {
                ISCalculator.initialize(cryptocurrency, start, end);
                setExchangeSupportScore();
            }
        };

        ThreadWithOnFinished ohlcThread = new ThreadWithOnFinished() {
            @Override
            public void doRun() {
                aisToolkit.fetchOHLC(base.getReloadOHLC().isSelected());
            }
        };

        ThreadWithOnFinished tweetFetchingThread = new ThreadWithOnFinished() {
            @Override
            public void doRun() {
                if (base.getFetchTweetsFromApi().isSelected()) {
                    aisToolkit.fetchAllTweetsFromStartToEnd();
                }
            }
        };

        ThreadWithOnFinished dataframeThread = new ThreadWithOnFinished() {
            @Override
            public void doRun() {
                if (base.getCreateDataframes().isSelected()) {
                    aisToolkit.deleteDataframes();
                    aisToolkit.createDataframes();
                }
            }
        };

        ThreadWithOnFinished priceActionThread = new ThreadWithOnFinished() {
            @Override
            public void doRun() {
                base.setStatusMessage("Filling Price Action Data");
                fillHighestVelocityPriceAction(true);
                fillHighestVelocityPriceAction(false);
                fillChronologicalPriceAction(true);
                fillChronologicalPriceAction(false);
            }
        };

        ThreadWithOnFinished tweetMappingThread = new ThreadWithOnFinished() {
            @Override
            public void doRun() {
                mapTweetsToUsers();
            }
        };

        exchangeSupportScoreThread.setOnFinished(ohlcThread::start);

        ohlcThread.setOnFinished(() -> {
            if (base.getFetchTweetsFromApi().isSelected())
                tweetFetchingThread.start();
            else dataframeThread.start();
        });

        tweetFetchingThread.setOnFinished(dataframeThread::start);

        dataframeThread.setOnFinished(() -> {
            priceActionThread.start();
            tweetMappingThread.start();
        });

        exchangeSupportScoreThread.start();
    }

    private static void applyConfiguration() {
        breakoutThresholdFactor = Integer.parseInt(base.getBreakoutThresholdInput().getText());
        cryptocurrency = base.getCryptocurrencyInput().getText();
        pcc = Double.parseDouble(base.getPccInput().getText());
        twitterInfluenceFactor = Double.parseDouble(base.getTwitterInfluenceFactor().getText());
        resultLimit = Integer.parseInt(base.getResultLimit().getText());
        minTweets = Integer.parseInt(base.getMinimumTweets().getText());
        windowSize = Integer.parseInt(base.getSlidingWindowSizeInput().getText());
        twitterInfluenceFactor = Double.parseDouble(base.getTwitterInfluenceFactor().getText());
        onlyUseOriginalTweets = base.getOnlyUseOriginalTweets().isSelected();
        onlyUseVerifiedUsers = base.getOnlyUseVerifiedUsers().isSelected();
        base.setStatusMessage("Started");
    }


    public static void log(String log) {
        System.out.println(log);
        base.log(log);
    }

    private static void initializeAISToolkit() throws IOException, ParseException {
        aisToolkit = new AISToolkit();
        aisToolkit.setCryptocurrency(cryptocurrency);
        aisToolkit.setTicker(base.getTickerInput().getText());
        aisToolkit.setQuery(cryptocurrency + " OR " + base.getTickerInput().getText());
        aisToolkit.setPCC(pcc);
        aisToolkit.setWindowSize(windowSize);
        aisToolkit.setBreakoutThreshold(breakoutThresholdFactor);
        aisToolkit.setTwitterInfluenceFactor(twitterInfluenceFactor);
        // TODO REMOVE, don't want to enter manually every time
        final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");
        start = timestampCreator.parse(PropertiesLoader.loadProperty("start")).getTime() / 1000;
        end = timestampCreator.parse(PropertiesLoader.loadProperty("end")).getTime() / 1000;
        aisToolkit.setStart(start);
        aisToolkit.setEnd(end);

        /*
        aisToolkit.setStart(Timestamp.valueOf(base.getStartdatePicker().getValue().atStartOfDay()).getTime() / 1000);
        aisToolkit.setEnd(Timestamp.valueOf(base.getEnddatePicker().getValue().atStartOfDay()).getTime() / 1000);

         */

    }

    private static void setExchangeSupportScore() {
        Platform.runLater(() -> {
            base.getEssLabel().setText(String.valueOf(ISCalculator.calculateExchangeSupportScore(cryptocurrency)));
        });
    }

    private static void mapTweetsToUsers() {
        List<TweetMap> tweetMapList = aisToolkit.mapTweetsToUser(onlyUseOriginalTweets).stream().filter(entry -> entry.getTotalTweetCount() > Integer.parseInt(base.getMinimumTweets().getText())).sorted(Comparator.comparingDouble(o -> -o.getAIS())).limit(resultLimit).collect(Collectors.toList());

        if (onlyUseVerifiedUsers) {
            tweetMapList = tweetMapList.stream().filter(entry -> entry.getUser().isVerified()).collect(Collectors.toList());
        }

        for (TweetMap user : tweetMapList) {
            Platform.runLater(() -> base.getUserContent().getChildren().add(createUserItem(user)));
        }
    }

    private static PriceActionItem createPriceActionItem(Pair<OHLC, OHLCStatistics> pair, Long timestampRange, boolean daily) {
        OHLC ohlc = pair.getKey();

        return new PriceActionItem(pair, Formatter.prettyFormatRange(ohlc.getTime() * 1000, (ohlc.getTime() + timestampRange) * 1000), breakoutThresholdFactor, aisToolkit.isAnomaly(pair), true, ISCalculator.calculateInfluenceabilityScore(cryptocurrency, pcc, ohlc.getTime()), () -> new Thread(() -> fillTweetsFromTimeframe(ohlc.getTime(), daily)).start());
    }

    private static UserItem createUserItem(TweetMap tweetMap) {
        return new UserItem(tweetMap.getUser(), tweetMap, () -> new Thread(() -> fillTweetsFromUser(tweetMap)).start());
    }

    private static void fillTweetsFromTimeframe(Long timestamp, boolean daily) {
        Platform.runLater(() -> {
            base.setStatusMessage("Showing all Tweets from " + Formatter.prettyFormatRange(timestamp * 1000, (timestamp + DURATION_HOUR) * 1000));
            base.getSelectedTweetContent().getChildren().clear();
        });

        List<DFTweet> dfTweets = daily ? aisToolkit.getTweetsFromDailyTimeframe(timestamp).stream().sorted(Comparator.comparingDouble(t -> -t.getEngagementShare())).limit(resultLimit).toList() : aisToolkit.getTweetsFromHourlyTimeframe(timestamp).stream().sorted(Comparator.comparingDouble(t -> -t.getEngagementShare())).toList();

        for (DFTweet t : dfTweets) {
            Platform.runLater(() -> base.getSelectedTweetContent().getChildren().add(new TweetItem(t)));
        }
    }

    private static void fillTweetsFromUser(TweetMap tweetMap) {
        Platform.runLater(() -> {
            base.setStatusMessage("Showing " + tweetMap.getTweets().size() + " Tweets from " + tweetMap.getUser().getUsername());
            base.getSelectedTweetContent().getChildren().clear();
        });
        for (DFTweet t : tweetMap.getTweets().stream().sorted(Comparator.comparingDouble(t -> -t.getAssociatedOutbreakMagnitude())).toList()) {
            Platform.runLater(() -> base.getSelectedTweetContent().getChildren().add(new TweetItem(t)));
        }
    }


    private static void fillHighestVelocityPriceAction(boolean daily) {
        for (Pair<OHLC, OHLCStatistics> stats : aisToolkit.getCandlesSortedByOutbreakMagnitude(daily).stream().limit(resultLimit).toList()) {
            Platform.runLater(() -> {
                if (daily)
                    base.getPriceActionHighestDailyVelocityContent().getChildren().add(createPriceActionItem(stats, DURATION_DAY, true));
                else
                    base.getPriceActionHighestHourlyVelocityContent().getChildren().add(createPriceActionItem(stats, DURATION_HOUR, false));
            });
        }
    }

    private static void fillChronologicalPriceAction(boolean daily) {
        for (Pair<OHLC, OHLCStatistics> stats : aisToolkit.getCandlesSortedChronologically(daily).stream().limit(resultLimit).toList()) {
            Platform.runLater(() -> {
                if (daily)
                    base.getPriceActionChronologicalDailyContent().getChildren().add(createPriceActionItem(stats, DURATION_DAY, false));
                else
                    base.getPriceActionChronologicalHourlyContent().getChildren().add(createPriceActionItem(stats, DURATION_HOUR, true));
            });
        }
    }


}
