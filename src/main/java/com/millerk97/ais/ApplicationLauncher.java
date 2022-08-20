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
        /*


        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLauncher.class.getResource("/com/millerk97/fxml/FXBaseApplication.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
*/
    }


}