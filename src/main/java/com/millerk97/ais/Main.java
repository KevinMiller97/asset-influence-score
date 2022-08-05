package com.millerk97.ais;

import com.millerk97.ais.cryptocompare.calc.AnomalyDay;
import com.millerk97.ais.cryptocompare.calc.SlidingWindow;
import com.millerk97.ais.cryptocompare.constant.Timeframe;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.impl.DataFetcher;
import com.millerk97.ais.twitter.TweetFetcher;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "UTC");
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Long start = formatter.parse("01.01.2020").getTime() / 1000;
            Long end = formatter.parse("10.07.2022").getTime() / 1000;
            // window size 14; threshold 2 works very well
            SlidingWindow window = new SlidingWindow(DataFetcher.getDailyOHLC("dogecoin", end.intValue()), 14, 2, Timeframe.DAYS_1);

            List<OHLC> anomalyDaysOHLC = window.findAnomalies(start, false);

            List<AnomalyDay> anomalyDays = new ArrayList<>();
            anomalyDaysOHLC.stream().map(ohlc -> anomalyDays.add(new AnomalyDay(DataFetcher.getHourlyOHLCsForDay(ohlc), 1.9))).collect(Collectors.toList());

            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            isoFormatter.setTimeZone(tz);

            for (AnomalyDay day : anomalyDays) {
                List<OHLC> anomalies = day.findAnomalies(false);
                for (OHLC ohlc : anomalies) {
                    Date startDate = new Date(ohlc.getTime() * 1000);
                    Date endDate = new Date((ohlc.getTime() + 3600) * 1000);
                    TweetFetcher.fetchTweets("Dogecoin OR Doge", isoFormatter.format(startDate), isoFormatter.format(endDate));
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Startscreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        */
    }
}