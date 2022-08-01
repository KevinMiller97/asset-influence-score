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
import java.util.stream.Collectors;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // necessary because of DST and because Vienna is in GMT+2, all data is provided in GMT
        System.setProperty("user.timezone", "GMT");

        //ISCalculator.calculateInfluencabilityScore("dogecoin");
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Long start = formatter.parse("01.01.2020").getTime() / 1000;
            Long end = formatter.parse("10.07.2022").getTime() / 1000;
            // window size 14; threshold 2 works very well
            SlidingWindow window = new SlidingWindow(DataFetcher.getDailyOHLC("dogecoin", end.intValue()), 14, 2, Timeframe.DAYS_1);

            List<OHLC> anomalyDaysOHLC = window.findAnomalies(start, true);

            List<AnomalyDay> anomalyDays = new ArrayList<>();
            anomalyDaysOHLC.stream().map(ohlc -> anomalyDays.add(new AnomalyDay(DataFetcher.getHourlyOHLCsForDay(ohlc), 1.9))).collect(Collectors.toList());

            for (AnomalyDay day : anomalyDays) {
                // day.findAnomalies(true);
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