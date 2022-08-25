package com.millerk97.ais;

import com.millerk97.ais.controller.FlowController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class ApplicationLauncher extends Application {


    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FlowController.init(stage);
    }


}