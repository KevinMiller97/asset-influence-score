package com.millerk97.ais.controller;

import com.millerk97.ais.coingecko.impl.ISCalculator;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLCStatistics;
import com.millerk97.ais.dataframe.model.DFTweet;
import com.millerk97.ais.dataframe.model.TweetMap;
import com.millerk97.ais.fxgui.FXBaseApplication;
import com.millerk97.ais.fxgui.components.PriceActionItem;
import com.millerk97.ais.fxgui.components.TweetItem;
import com.millerk97.ais.fxgui.components.UserItem;
import com.millerk97.ais.util.Formatter;
import com.millerk97.ais.util.PropertiesLoader;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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


    public static void init(Stage stage) {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "UTC");
        Scene scene = new Scene(base);
        stage.setWidth(1300);
        stage.setHeight(1030);
        stage.setTitle("AIS Calculator");
        stage.setScene(scene);
        base.getStartButton().setOnAction(action -> run());
        stage.show();
    }

    public static void run() {
        breakoutThresholdFactor = Integer.parseInt(base.getBreakoutThresholdInput().getText());
        cryptocurrency = base.getCryptocurrencyInput().getText();
        pcc = Double.parseDouble(base.getPccInput().getText());
        twitterInfluenceFactor = Double.parseDouble(base.getTwitterInfluenceFactor().getText());
        resultLimit = Integer.parseInt(base.getResultLimit().getText());
        minTweets = Integer.parseInt(base.getMinimumTweets().getText());
        windowSize = Integer.parseInt(base.getSlidingWindowSizeInput().getText());
        twitterInfluenceFactor = Double.parseDouble(base.getTwitterInfluenceFactor().getText());
        base.setStatusMessage("Started");

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
                mapTweetsToUsers(base.getOnlyUseOriginalTweets().isSelected());
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


    public static void log(String log) {
        System.out.println(log);
        base.log(log);
    }

    private static void initializeAISToolkit() throws IOException, ParseException {
        aisToolkit = new AISToolkit();
        aisToolkit.setCryptocurrency(cryptocurrency);
        aisToolkit.setTicker(base.getTickerInput().getText());
        aisToolkit.setQuery(cryptocurrency + " OR " + base.getTickerInput().getText());
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

    private static void mapTweetsToUsers(boolean onlyUseOriginalTweets) {
        List<TweetMap> tweetMapList = aisToolkit.mapTweetsToUser(onlyUseOriginalTweets).stream().filter(entry -> entry.getTotalTweetCount() > Integer.parseInt(base.getMinimumTweets().getText())).sorted(Comparator.comparingDouble(o -> -o.getAIS())).limit(resultLimit).collect(Collectors.toList());

        if (base.getOnlyUseVerifiedUsers().isSelected()) {
            tweetMapList = tweetMapList.stream().filter(entry -> entry.getUser().isVerified()).collect(Collectors.toList());
        }

        for (TweetMap user : tweetMapList) {
            Platform.runLater(() -> base.getUserContent().getChildren().add(createUserItem(user)));
        }
    }

    private static PriceActionItem createPriceActionItem(Pair<OHLC, OHLCStatistics> pair, Long timestampRange, boolean showTweetsButtonVisible) {
        OHLC ohlc = pair.getKey();

        return new PriceActionItem(pair, Formatter.prettyFormatRange(ohlc.getTime() * 1000, (ohlc.getTime() + timestampRange) * 1000), breakoutThresholdFactor, aisToolkit.isAnomaly(pair), showTweetsButtonVisible, ISCalculator.calculateInfluenceabilityScore(cryptocurrency, pcc, ohlc.getTime()), () -> new Thread(() -> fillTweetsFromTimeframe(ohlc.getTime())).start());
    }

    private static UserItem createUserItem(TweetMap tweetMap) {
        return new UserItem(tweetMap.getUser(), tweetMap, () -> new Thread(() -> fillTweetsFromUser(tweetMap)).start());
    }

    private static void fillTweetsFromTimeframe(Long timestamp) {
        Platform.runLater(() -> {
            base.setStatusMessage("Showing all Tweets from " + Formatter.prettyFormatRange(timestamp * 1000, (timestamp + DURATION_HOUR) * 1000));
            base.getSelectedTweetContent().getChildren().clear();
        });
        for (DFTweet t : aisToolkit.getTweetsFromTimeframe(timestamp).stream().sorted(Comparator.comparingDouble(t -> -t.getEngagementShare())).toList()) {
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
                    base.getPriceActionHighestDailyVelocityContent().getChildren().add(createPriceActionItem(stats, DURATION_DAY, false));
                else
                    base.getPriceActionHighestHourlyVelocityContent().getChildren().add(createPriceActionItem(stats, DURATION_HOUR, true));
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
