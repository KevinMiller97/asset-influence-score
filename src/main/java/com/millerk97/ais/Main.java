package com.millerk97.ais;

import com.millerk97.ais.cryptocompare.calc.AnomalyDay;
import com.millerk97.ais.cryptocompare.calc.SlidingWindow;
import com.millerk97.ais.cryptocompare.constant.Timeframe;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.impl.DataFetcher;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        //ISCalculator.calculateInfluencabilityScore("dogecoin");

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Long start = formatter.parse("01.01.2020").getTime() / 1000;
            System.out.println(start);
            Long end = formatter.parse("10.07.2022").getTime() / 1000;
            System.out.println(end);
            // window size 14; threshold 2 works very well
            SlidingWindow window = new SlidingWindow(DataFetcher.getDailyOHLC("dogecoin", end.intValue()), 14, 2, Timeframe.DAYS_1);

            List<OHLC> anomalyDaysOHLC = window.findAnomalies(true);

            //System.out.println(CryptoCompareReader.getHourlyOHLCsForDay(anomalyDaysOHLC.get(0)));

            List<AnomalyDay> anomalyDays = new ArrayList<>();
            anomalyDaysOHLC.stream().map(ohlc -> anomalyDays.add(new AnomalyDay(DataFetcher.getHourlyOHLCsForDay(ohlc), 2))).collect(Collectors.toList());

            for (AnomalyDay day : anomalyDays) {
                day.findAnomalies(true);
            }

            /*
            List<OHLC> hourliesOfAnomalyDays = CryptoCompareReader.getHourlyOHLCForDays(window.findAnomalies(false));
            SlidingWindow hourlyWindow = new SlidingWindow(hourliesOfAnomalyDays, 8, 3, Timeframe.HOURS_1);
            hourlyWindow.findAnomalies(true);
            */

            /*
            for (int i = 0; i < 100; i++) {
                System.out.printf("%.9f", window.calculateStandardDeviation());
                window.advanceWindow();
            }
             */
            //System.out.printf("%.9f", window.calculateStandardDeviation());

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