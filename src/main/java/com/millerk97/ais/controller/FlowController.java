package com.millerk97.ais.controller;

import com.millerk97.ais.coingecko.impl.ISCalculator;
import com.millerk97.ais.cryptocompare.calc.SlidingWindow;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLCStatistics;
import com.millerk97.ais.dataframe.model.DFTweet;
import com.millerk97.ais.dataframe.model.DFUser;
import com.millerk97.ais.dataframe.model.TweetMap;
import com.millerk97.ais.fxgui.FXBaseApplication;
import com.millerk97.ais.fxgui.components.PriceActionItem;
import com.millerk97.ais.fxgui.components.TweetItem;
import com.millerk97.ais.fxgui.components.UserItem;
import com.millerk97.ais.util.PropertiesLoader;
import com.millerk97.ais.util.TimeFormatter;
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
    private static Stage stage;
    private static AISToolkit aisToolkit;

    public static void init(Stage stage) {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "UTC");
        FlowController.stage = stage;
        Scene scene = new Scene(base);
        stage.setWidth(1300);
        stage.setHeight(1030);
        stage.setScene(scene);
        base.getStartButton().setOnAction(action -> run());
        stage.show();
    }

    public static void run() {

        base.setStatusMessage("Started");
        Platform.runLater(() -> {
            // clear everything in case of multiple executions
            base.getPriceActionHighestDailyVelocityContent().getChildren().clear();
            base.getPriceActionHighestHourlyVelocityContent().getChildren().clear();
            base.getPriceActionChronologicalDailyContent().getChildren().clear();
            base.getPriceActionChronologicalHourlyContent().getChildren().clear();
            base.getUserContent().getChildren().clear();
            base.getMostPopularTweetContent().getChildren().clear();
        });

        try {
            initializeAIS();
        } catch (IOException | ParseException e) {
            log(e.getMessage());
            e.printStackTrace();
        }

        aisToolkit.getMessageProperty().addListener((observableValue, oldValue, message) -> {
            Platform.runLater(() -> {
                base.setStatusMessage(message);
            });
        });

        ThreadWithOnFinished isCalculatorThread = new ThreadWithOnFinished() {
            @Override
            public void doRun() {
                calculateInfluencabilityScore();
            }
        };

        // Threads prevent the GUI from freezing
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
                if (base.getDeleteDataframes().isSelected()) {
                    aisToolkit.deleteDataframes();
                }

                if (base.getDeleteDataframes().isSelected() || base.getCreateDataframes().isSelected()) {
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
                if (base.getMapTweets().isSelected()) {
                    mapTweets();
                }
            }
        };

        isCalculatorThread.setOnFinished(() -> {
            ohlcThread.start();
        });

        ohlcThread.setOnFinished(() -> {
            if (base.getFetchTweetsFromApi().isSelected())
                tweetFetchingThread.start();
            else dataframeThread.start();
        });

        tweetFetchingThread.setOnFinished(() -> {
            dataframeThread.start();
        });

        dataframeThread.setOnFinished(() -> {
            priceActionThread.start();
            tweetMappingThread.start();
        });

        isCalculatorThread.start();
    }


    public static void log(String log) {
        System.out.println(log);
        base.log(log);
    }

    private static void initializeAIS() throws IOException, ParseException {
        aisToolkit = new AISToolkit();
        aisToolkit.setCryptocurrency(base.getCryptocurrencyInput().getText());
        aisToolkit.setTicker(base.getTickerInput().getText());
        aisToolkit.setQuery(base.getCryptocurrencyInput().getText() + " OR " + base.getTickerInput().getText());
        aisToolkit.setWindowSize(Integer.parseInt(base.getSlidingWindowSizeInput().getText()));
        aisToolkit.setBreakoutThreshold(Integer.parseInt(base.getBreakoutThresholdInput().getText()));

        // TODO REMOVE, don't want to enter manually every time
        final SimpleDateFormat timestampCreator = new SimpleDateFormat("dd.MM.yyyy");
        aisToolkit.setStart(timestampCreator.parse(PropertiesLoader.loadProperty("start")).getTime() / 1000);
        aisToolkit.setEnd(timestampCreator.parse(PropertiesLoader.loadProperty("end")).getTime() / 1000);

        /*
        aisToolkit.setStart(Timestamp.valueOf(base.getStartdatePicker().getValue().atStartOfDay()).getTime() / 1000);
        aisToolkit.setEnd(Timestamp.valueOf(base.getEnddatePicker().getValue().atStartOfDay()).getTime() / 1000);

         */
    }

    private static void calculateInfluencabilityScore() {
        Platform.runLater(() -> {
            String cryptocurrency = base.getCryptocurrencyInput().getText();

            base.getInfluencabilityScoreLabel().setText(String.valueOf(ISCalculator.calculateInfluencabilityScore(cryptocurrency, Double.parseDouble(base.getPccInput().getText()), base.getReloadOHLC().isSelected())));

            base.getEssLabel().setText(String.valueOf(ISCalculator.calculateExchangeSupportScore(cryptocurrency, false)));

            base.getMcrLabel().setText(String.valueOf(ISCalculator.calculateMCR(cryptocurrency, false)));

            base.getIsCalculationLabel().setText(ISCalculator.getInfluenceabilityScoreCalculationString(cryptocurrency, Double.parseDouble(base.getPccInput().getText())));
        });
    }

    private static void fillMostPopularTweets(TweetMap tweetMap) {
        Platform.runLater(() -> {
            base.setStatusMessage("Showing " + tweetMap.getTweets().size() + " Tweets from " + tweetMap.getUser().getUsername());
            base.getMostPopularTweetContent().getChildren().clear();
        });
        for (DFTweet t : tweetMap.getTweets().stream().sorted(Comparator.comparingDouble(t -> -t.getAssociatedVelocity())).collect(Collectors.toList())) {
            Platform.runLater(() -> {
                base.getMostPopularTweetContent().getChildren().add(createTweetItem(t));
            });
        }
    }

    private static void mapTweets() {
        List<TweetMap> tweetMapList = aisToolkit.mapTweets().stream().filter(entry -> entry.getTotalTweetCount() > Integer.parseInt(base.getMinimumTweets().getText())).sorted(Comparator.comparingDouble(o -> -o.getWeightedAvgMagnitude())).limit(Long.parseLong(base.getResultLimit().getText())).collect(Collectors.toList());

        if (base.getOnlyUseVerifiedUsers().isSelected()) {
            tweetMapList = tweetMapList.stream().filter(entry -> entry.getUser().isVerified()).collect(Collectors.toList());
        }

        for (TweetMap user : tweetMapList) {
            Platform.runLater(() -> {
                base.getUserContent().getChildren().add(createUserItem(user));
            });
        }
    }

    private static TweetItem createTweetItem(DFTweet tweet) {
        return new TweetItem(tweet.getId(), tweet.getUser().getUsername(), tweet.getText(), tweet.getPublicMetrics().getLikeCount(), tweet.getPublicMetrics().getRetweetCount(), tweet.getPublicMetrics().getReplyCount(), tweet.getCreatedAt(), tweet.getAssociatedVelocity());
    }

    private static PriceActionItem createPriceActionItem(Pair<OHLC, OHLCStatistics> pair, Long timestampRange) {
        OHLC ohlc = pair.getKey();
        OHLCStatistics stat = pair.getValue();
        return new PriceActionItem(TimeFormatter.prettyFormatRange(ohlc.getTime() * 1000, (ohlc.getTime() + timestampRange) * 1000), ohlc.getOpen(), ohlc.getClose(), ohlc.getHigh(), ohlc.getLow(), ohlc.getVolumeTo(), SlidingWindow.calculateOHLCImpact(ohlc), Integer.parseInt(base.getBreakoutThresholdInput().getText()) * stat.getMeanFluctuation(), AISToolkit.calculateOutbreakMagnitude(pair), aisToolkit.isAnomaly(pair));
    }

    private static UserItem createUserItem(TweetMap tweetMap) {
        DFUser user = tweetMap.getUser();
        return new UserItem(user.getUsername(), user.getCreatedAt(), user.getPublicMetrics().getFollowerCount(), (int) tweetMap.getTotalTweetCount(), (int) tweetMap.getRegularTweetCount(), (int) tweetMap.getAnomalyTweetCount(), tweetMap.computeAnomalyRatio(), tweetMap.getAvgMagnitude(), tweetMap.getWeightedAvgMagnitude(), () -> {
            new Thread(() -> {
                fillMostPopularTweets(tweetMap);
            }).start();
        });
    }

    private static void fillHighestVelocityPriceAction(boolean daily) {
        for (Pair<OHLC, OHLCStatistics> stats : aisToolkit.getCandlesSortedByOutbreakMagnitude(daily).stream().limit(Integer.parseInt(base.getResultLimit().getText())).collect(Collectors.toList())) {
            Platform.runLater(() -> {
                if (daily)
                    base.getPriceActionHighestDailyVelocityContent().getChildren().add(createPriceActionItem(stats, DURATION_DAY));
                else
                    base.getPriceActionHighestHourlyVelocityContent().getChildren().add(createPriceActionItem(stats, DURATION_HOUR));
            });
        }
    }

    private static void fillChronologicalPriceAction(boolean daily) {
        for (Pair<OHLC, OHLCStatistics> stats : aisToolkit.getCandlesSortedChronologically(daily).stream().limit(Integer.parseInt(base.getResultLimit().getText())).collect(Collectors.toList())) {
            Platform.runLater(() -> {
                if (daily)
                    base.getPriceActionChronologicalDailyContent().getChildren().add(createPriceActionItem(stats, DURATION_DAY));
                else
                    base.getPriceActionChronologicalHourlyContent().getChildren().add(createPriceActionItem(stats, DURATION_HOUR));
            });
        }
    }


}
