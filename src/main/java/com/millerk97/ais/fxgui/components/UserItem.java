package com.millerk97.ais.fxgui.components;

import com.millerk97.ais.dataframe.model.DFUser;
import com.millerk97.ais.dataframe.model.TweetMap;
import com.millerk97.ais.util.Formatter;
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
    private Label avgEngagementPercentageLabel;
    @FXML
    private Label avgMagnitudeLabel;
    @FXML
    private Label avgAttrMagnitudeLabel;
    @FXML
    private Label avgAttrMagnitudeAdjTwitterLabel;
    @FXML
    private Label medianMagnitudeLabel;
    @FXML
    private Label medianAttrMagnitudeLabel;
    @FXML
    private Label medianAttrMagnitudeAdjTwitterLabel;
    @FXML
    private Label aisLabel;
    @FXML
    private Button showTweetsButton;

    public UserItem(DFUser user, TweetMap tweetMap, Runnable onShowTweetsAction) {
        ScreenHelper.loadFXML(this, this);
        usernameLabel.setText(user.getUsername());
        createdAtLabel.setText(user.getCreatedAt());
        followerCountLabel.setText(Formatter.formatNumber(user.getPublicMetrics().getFollowerCount()));
        totalTweetCountLabel.setText(Formatter.formatNumber((int) tweetMap.getTotalTweetCount()));
        tweetcountStandardLabel.setText(Formatter.formatNumber((int) tweetMap.getRegularTweetCount()));
        tweetcountAnomalyLabel.setText(Formatter.formatNumber((int) tweetMap.getAnomalyTweetCount()));
        anomalyRatioLabel.setText(Formatter.formatNumberDecimal(tweetMap.getAnomalyRatio()));
        avgEngagementPercentageLabel.setText(Formatter.formatNumberDecimal(tweetMap.getAvgEngagement() * 100));
        avgMagnitudeLabel.setText(Formatter.formatNumberDecimal(tweetMap.getAvgMagnitude()));
        avgAttrMagnitudeLabel.setText(Formatter.formatNumberDecimal(tweetMap.getAvgAttributableMagnitude()));
        avgAttrMagnitudeAdjTwitterLabel.setText(Formatter.formatNumberDecimal(tweetMap.getAvgAttributableMagnitudeAdjustedForTwitterInfluence()));
        medianMagnitudeLabel.setText(Formatter.formatNumberDecimal(tweetMap.getMedianMagnitude()));
        medianAttrMagnitudeLabel.setText(Formatter.formatNumberDecimal(tweetMap.getMedianAttributableMagnitude()));
        medianAttrMagnitudeAdjTwitterLabel.setText(Formatter.formatNumberDecimal(tweetMap.getMedianAttributableMagnitudeAdjustedForTwitterInfluence()));
        aisLabel.setText(Formatter.formatNumberDecimal(tweetMap.getAIS()));
        showTweetsButton.setOnAction(action -> onShowTweetsAction.run());
        if (user.isVerified()) {
            root.setStyle("-fx-background-color: #00acee;");
        }
    }

}
