package com.millerk97.ais;

import com.millerk97.ais.coingecko.ess.ESSCalculator;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        ESSCalculator.calculateExchangeSupportScore("dogecoin");

        /*
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Startscreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        */
    }
}