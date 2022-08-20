package com.millerk97.ais.fxgui.components;

import com.millerk97.ais.util.ScreenHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class UserItem extends GridPane {

    @FXML
    private GridPane root;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private Label followerCountLabel;
    @FXML
    private Label totalTweetCountLabel;
    @FXML
    private Label tweetcountStandardLabel;
    @FXML
    private Label tweetcountAnomalyLabel;
    @FXML
    private Label anomalyRatioLabel;
    @FXML
    private Label avgVelocityLabel;
    @FXML
    private Label weightedAvgVelocityLabel;
    @FXML
    private Button showTweetsButton;

    public UserItem(String username, String createdAt, int followerCount, int totalTweetCount, int standardTweets, int anomalyTweets, double anomalyRatio, double avgVelocity, double weightedAvgVelocity, Runnable onShowTweetsAction) {
        ScreenHelper.loadFXML(this, this);
        usernameLabel.setText(username);
        createdAtLabel.setText(createdAt);
        followerCountLabel.setText(String.valueOf(followerCount));
        totalTweetCountLabel.setText(String.valueOf(totalTweetCount));
        tweetcountStandardLabel.setText(String.valueOf(standardTweets));
        tweetcountAnomalyLabel.setText(String.valueOf(anomalyTweets));
        anomalyRatioLabel.setText(String.valueOf(anomalyRatio));
        avgVelocityLabel.setText(String.valueOf(avgVelocity));
        weightedAvgVelocityLabel.setText(String.valueOf(weightedAvgVelocity));
        showTweetsButton.setOnAction(action -> onShowTweetsAction.run());
    }

}
