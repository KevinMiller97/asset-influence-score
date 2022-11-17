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
import com.millerk97.ais.twitter.TweetFetcher;
import com.millerk97.ais.util.Formatter;
import com.millerk97.ais.util.PropertiesLoader;
import com.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FlowController {

    private static final FXBaseApplication base = new FXBaseApplication();
    private static final Long DURATION_DAY = 86340L; // 23:59
    private static final Long DURATION_HOUR = 3600L;
    private static final int resultLimit = 400;
    private static AISToolkit aisToolkit;
    private static Long start;
    private static Long end;
    private static int breakoutThresholdFactor;
    private static int minimumTweets;
    private static String cryptocurrency;
    private static double pcc;
    private static int windowSize;
    private static long TRADE_START, TRADE_END = 0L;

    public static void init(Stage stage) {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "UTC");
        Scene scene = new Scene(base);
        stage.setWidth(1300);
        stage.setHeight(1030);
        stage.setTitle("AIS Calculator");
        stage.setScene(scene);

        Runnable setup = () -> {
            base.setStatusMessage("Started - Applying configuration and preparing data");
            initialize();
            aisToolkit.getMessageProperty().addListener((observableValue, oldValue, message) -> Platform.runLater(() -> base.setStatusMessage(message)));
            String bearer;
            if (base.getBearerToken().getText().equals("")) {
                bearer = PropertiesLoader.loadBearerToken();
                base.getBearerToken().setText(bearer);
            } else {
                bearer = base.getBearerToken().getText();
            }
            TweetFetcher.setBearerToken(bearer);
        };

        base.getStartButton().setOnAction(action -> new Thread(() -> {
            setup.run();
            try {
                runAISCalculation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start());

        base.getTradeStrategyButton().setOnAction(action -> new Thread(() -> {
            setup.run();
            final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");
            try {
                start = timestampCreator.parse("01.01.2020").getTime() / 1000;
                TRADE_START = timestampCreator.parse("05.01.2021").getTime() / 1000;
                TRADE_START = (Timestamp.valueOf(base.getTradeStartPicker().getValue().atStartOfDay()).getTime() / 1000);
            } catch (ParseException | NullPointerException e) {
                log("no start date entered - using 05.01.2021 as trade start");
            }
            try {
                TRADE_END = timestampCreator.parse("30.06.2022").getTime() / 1000;
                TRADE_END = (Timestamp.valueOf(base.getTradeEndPicker().getValue().atStartOfDay()).getTime() / 1000);
            } catch (ParseException | NullPointerException e) {
                log("no end date entered - using 30.06.2022 as trade end");
            }

            aisToolkit.setStart(start);
            aisToolkit.setEnd(TRADE_END);

            applyTrade(windowSize, breakoutThresholdFactor, minimumTweets);
        }).start());
        stage.show();
    }


    private static String round(double input) {
        return String.valueOf(new BigDecimal(input).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    public static void runAISCalculation() throws InterruptedException {
        Platform.runLater(() -> {
            // clear everything in case of multiple executions
            base.getPriceActionHighestDailyVelocityContent().getChildren().clear();
            base.getPriceActionHighestHourlyVelocityContent().getChildren().clear();
            base.getPriceActionChronologicalDailyContent().getChildren().clear();
            base.getPriceActionChronologicalHourlyContent().getChildren().clear();
            base.getUserContent().getChildren().clear();
            base.getSelectedTweetContent().getChildren().clear();
        });

        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch isLatch = new CountDownLatch(1);
        // IS Calculator
        executorService.submit(() -> {
            ISCalculator.initialize(cryptocurrency, start, end, base.getReloadOHLC().isSelected());
            isLatch.countDown();
        });
        isLatch.await();

        CountDownLatch preparationLatch = new CountDownLatch(2);
        // OHLC
        executorService.submit(() -> {
            aisToolkit.fetchOHLC(base.getReloadOHLC().isSelected());
            preparationLatch.countDown();
        });
        // Bitcoin Dataframes
        executorService.submit(() -> {
            if (!cryptocurrency.equalsIgnoreCase("bitcoin")) {
                AISToolkit.prepareBitcoinMagnitudes(start, end);
            }
            preparationLatch.countDown();
        });
        // Dateframe deletion
        executorService.submit(() -> {
            if (base.getCreateDataframes().isSelected()) {
                //    aisToolkit.deleteDataframes();
            }
            preparationLatch.countDown();
        });
        preparationLatch.await();

        CountDownLatch dataframeLatch = new CountDownLatch(1);
        executorService.submit(() -> {
            if (base.getCreateDataframes().isSelected()) {
                aisToolkit.createDataframes();
            }
            dataframeLatch.countDown();
        });
        dataframeLatch.await();

        // no more latches for GUI
        executorService.submit(() -> {
            base.setStatusMessage("Filling Price Action Data");
            fillHighestVelocityPriceAction(true);
            fillHighestVelocityPriceAction(false);
            fillChronologicalPriceAction(true);
            fillChronologicalPriceAction(false);
        });
        executorService.submit(() -> mapTweetsToUsers());
    }

    public static void log(String log) {
        System.out.println(log);
        base.log(log);
    }

    public static void applyTrade(int windowSize, int breakoutThresholdFactor, int minTweets) {
        aisToolkit.setBreakoutThreshold(breakoutThresholdFactor);

        final int limitTopUsers = 8;
        double investorAHoldings = 58824; // 10000 at 0.17 cents
        double investorBHoldings = 200000d;

        long hourlyTimestamp = TRADE_START;
        long dailyTimestamp = TRADE_START;

        List<String[]> tradingDays = new ArrayList<>();
        List<TweetMap> tweetMapList;

        List<Trader> traders = new ArrayList<>();
        traders.add(new Trader(0L));
        traders.add(new Trader(86400L));
        traders.add(new Trader(172800L));
        traders.add(new Trader(259200L));
        traders.add(new Trader(604800L));
        traders.add(new Trader(1209600L));

        Map<String, List<String[]>> aisDevelopment = new HashMap<>();

        while (dailyTimestamp <= TRADE_END) {
            tweetMapList = aisToolkit.mapTweetsToUser(true, dailyTimestamp).stream().filter(entry -> entry.getTotalTweetCount() >= minTweets).filter(entry -> entry.getUser().isVerified()).filter(entry -> entry.getAIS() > 0).sorted(Comparator.comparingDouble(o -> -o.getAIS())).limit(limitTopUsers).collect(Collectors.toList());
            System.out.println("Current AIS Leaderboard:");
            for (TweetMap tm : tweetMapList) {
                String username = tm.getUser().getUsername();
                System.out.println(String.format("%s, %s", username, tm.getAIS()));
                aisDevelopment.computeIfAbsent(username, k -> new ArrayList<>());
                aisDevelopment.get(username).add(new String[]{Formatter.prettyFormatShort(dailyTimestamp * 1000), round(tm.getAIS())});
            }
            while (hourlyTimestamp + 3600 <= dailyTimestamp + 86400) {
                Dataframe df = DataframeUtil.getDataframe(hourlyTimestamp, cryptocurrency);
                // iterate over top 5 users
                if (df != null) {
                    for (TweetMap tm : tweetMapList) {
                        for (DFTweet t : df.getTweets()) {
                            if (t.getUser().getUsername().equals(tm.getUser().getUsername())) {
                                // this user tweeted on this day
                                double entryPrice = (df.getOhlc().getOpen() + (df.getOhlc().getClose() - df.getOhlc().getOpen()) / 5); // 20% premium
                                for (Trader trader : traders) {
                                    double availableBudget = trader.balance * tm.getAIS() / 100;
                                    trader.balance -= availableBudget;
                                    double entryAmount = availableBudget / entryPrice;
                                    trader.currentHoldings += entryAmount;
                                    trader.latestEntry = dailyTimestamp;
                                }
                                log(String.format("Buying at entry price of %f because %s tweeted at %s", entryPrice, t.getUser().getUsername(), t.getCreatedAt()));
                            }
                        }
                    }
                }
                hourlyTimestamp += 3600;
            }
            // close open positions
            Dataframe dfClose = DataframeUtil.getDataframe(hourlyTimestamp, cryptocurrency);
            if (dfClose != null) {
                for (Trader t : traders) {
                    if (t.currentHoldings > 0) {
                        if (dailyTimestamp >= t.latestEntry + t.holdDuration) {
                            double tradeResult = t.currentHoldings * dfClose.getOhlc().getOpen();
                            t.balance += tradeResult;
                            t.currentHoldings = 0;
                        }
                    }
                }
                String[] tradingDayResults = {Formatter.prettyFormatShort(hourlyTimestamp * 1000), round(investorAHoldings * dfClose.getOhlc().getOpen()), round(investorBHoldings * dfClose.getOhlc().getOpen()), round(traders.get(0).balance + traders.get(0).currentHoldings * dfClose.getOhlc().getOpen()), round(traders.get(1).balance + traders.get(1).currentHoldings * dfClose.getOhlc().getOpen()), round(traders.get(2).balance + traders.get(2).currentHoldings * dfClose.getOhlc().getOpen()), round(traders.get(3).balance + traders.get(3).currentHoldings * dfClose.getOhlc().getOpen()), round(traders.get(4).balance + traders.get(4).currentHoldings * dfClose.getOhlc().getOpen()), round(traders.get(5).balance + traders.get(5).currentHoldings * dfClose.getOhlc().getOpen())};

                log(String.format("%s, (Inv A) %s, (Inv B) %s, (EOD) %s, (1D) %s, (2D) %s, (3D) %s, (1W) %s, (2W) %s", tradingDayResults));
                tradingDays.add(tradingDayResults);
            }
            dailyTimestamp += 86400;
        }
        try {
            new File("src/main/resources/com/millerk97/trade/").mkdirs();
            FileWriter outputfile = new FileWriter(String.format("src/main/resources/com/millerk97/trade/%s_%s_%s_w%d_b%d_t%d.csv", Formatter.prettyFormatShort(TRADE_START), Formatter.prettyFormatShort(TRADE_END), cryptocurrency, windowSize, breakoutThresholdFactor, minTweets));
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeAll(tradingDays);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initialize() {
        final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");
        breakoutThresholdFactor = Integer.parseInt(base.getBreakoutThresholdInput().getText());
        cryptocurrency = base.getCryptocurrencyInput().getText();
        pcc = Double.parseDouble(base.getPccInput().getText());
        windowSize = Integer.parseInt(base.getSlidingWindowSizeInput().getText());
        minimumTweets = Integer.parseInt(base.getMinimumTweets().getText());

        aisToolkit = new AISToolkit();
        aisToolkit.setCryptocurrency(cryptocurrency);
        aisToolkit.setTicker(base.getTickerInput().getText());
        aisToolkit.setQuery(cryptocurrency + " OR " + base.getTickerInput().getText());
        aisToolkit.setPCC(pcc);
        aisToolkit.setWindowSize(windowSize);
        aisToolkit.setBreakoutThreshold(breakoutThresholdFactor);
        try {
            start = timestampCreator.parse(PropertiesLoader.loadProperty("start")).getTime() / 1000;
            aisToolkit.setStart(start);
            end = timestampCreator.parse(PropertiesLoader.loadProperty("end")).getTime() / 1000;
            aisToolkit.setEnd(end);
            start = Timestamp.valueOf(base.getAisStartDatePicker().getValue().atStartOfDay()).getTime() / 1000;
            aisToolkit.setStart(start);
            end = Timestamp.valueOf(base.getAisEndDatePicker().getValue().atStartOfDay()).getTime() / 1000;
            aisToolkit.setEnd(end);
        } catch (IOException | ParseException e) {
            log(e.getMessage());
        }
    }

    private static void mapTweetsToUsers() {
        List<TweetMap> tweetMapList = aisToolkit.mapTweetsToUser(true, end).stream().filter(entry -> entry.getTotalTweetCount() > minimumTweets).filter(entry -> entry.getUser().isVerified()).sorted(Comparator.comparingDouble(o -> -o.getAIS())).limit(resultLimit).toList();

        for (TweetMap user : tweetMapList) {
            Platform.runLater(() -> base.getUserContent().getChildren().add(createUserItem(user)));
        }
    }

    private static PriceActionItem createPriceActionItem(Pair<OHLC, OHLCStatistics> pair, Long timestampRange, boolean daily) {
        OHLC ohlc = pair.getKey();

        return new PriceActionItem(pair, Formatter.prettyFormatRange(ohlc.getTime() * 1000, (ohlc.getTime() + timestampRange) * 1000), breakoutThresholdFactor, aisToolkit.isAnomaly(pair), true, ISCalculator.calculateInfluenceabilityScore(pcc, ohlc.getTime()), () -> new Thread(() -> fillTweetsFromTimeframe(ohlc.getTime(), daily)).start());
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

    static class Trader {
        public final Long holdDuration;
        public double balance = 10000d;
        public double currentHoldings = 0d;
        public Long latestEntry = 0L;

        public Trader(Long holdDuration) {
            this.holdDuration = holdDuration;
        }
    }


}
