package com.millerk97.ais;

import com.millerk97.ais.cryptowatch.impl.DataFetcher;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        //ISCalculator.calculateInfluencabilityScore("dogecoin");

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Long start = formatter.parse("01.01.2020").getTime() / 1000;
            Long end = formatter.parse("31.05.2022").getTime() / 1000;
            System.out.println(DataFetcher.getDailyOHLC("dogecoin", end.intValue(), start.intValue()));
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