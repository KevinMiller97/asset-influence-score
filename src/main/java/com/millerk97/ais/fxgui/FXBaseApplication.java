package com.millerk97.ais.fxgui;

import com.millerk97.ais.util.ScreenHelper;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Data;

@Data
public class FXBaseApplication extends StackPane {

    private final int maxConsoleItems = 300;
    @FXML
    private TextField cryptocurrencyInput;
    @FXML
    private TextField tickerInput;
    @FXML
    private TextField slidingWindowSizeInput;
    @FXML
    private TextField breakoutThresholdInput;
    @FXML
    private TextField pccInput;
    @FXML
    private DatePicker aisDatePicker;
    @FXML
    private Button startButton;
    @FXML
    private DatePicker tradeStartPicker;
    @FXML
    private DatePicker tradeEndPicker;
    @FXML
    private Button tradeStrategyButton;
    @FXML
    private VBox priceActionHighestDailyVelocityContent;
    @FXML
    private VBox priceActionChronologicalDailyContent;
    @FXML
    private VBox priceActionHighestHourlyVelocityContent;
    @FXML
    private VBox priceActionChronologicalHourlyContent;
    @FXML
    private VBox userContent;
    @FXML
    private VBox console;
    @FXML
    private TabPane tweetPane;
    @FXML
    private VBox selectedTweetContent;
    @FXML
    private CheckBox fetchTweetsFromApi;
    @FXML
    private CheckBox createDataframes;
    @FXML
    private CheckBox reloadOHLC;
    @FXML
    private TextField minimumTweets;
    @FXML
    private TextField bearerToken;
    @FXML
    private Label statusMessage;

    public FXBaseApplication() {
        ScreenHelper.loadFXML(this, this);
    }

    public void log(String log) {
        Platform.runLater(() -> {
            ObservableList<Node> c = console.getChildren();
            if (!(c.size() < maxConsoleItems)) {
                c.remove(0);
            }
            c.add(new Label(log));
        });
    }

    public void setStatusMessage(String message) {
        Platform.runLater(() -> {
            statusMessage.setText(message);
        });
    }

}
