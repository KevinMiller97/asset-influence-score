package com.millerk97.ais;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StartScreen {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}