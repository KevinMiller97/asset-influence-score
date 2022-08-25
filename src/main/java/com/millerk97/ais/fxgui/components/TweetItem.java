package com.millerk97.ais.fxgui.components;

import com.millerk97.ais.dataframe.model.DFTweet;
import com.millerk97.ais.util.Formatter;
import com.millerk97.ais.util.ScreenHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class TweetItem extends GridPane {
    @FXML
    private GridPane root;
    @FXML
    private Label tweetIdLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label textLabel;
    @FXML
    private Label likeCountLabel;
    @FXML
    private Label retweetLabel;
    @FXML
    private Label replyCountLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private Label velocityLabel;
    @FXML
    private Label engagementShareLabel;

    public TweetItem(DFTweet t) {
        ScreenHelper.loadFXML(this, this);
        tweetIdLabel.setText(t.getId());
        authorLabel.setText(t.getUser().getUsername());
        textLabel.setText(t.getText());
        likeCountLabel.setText(Formatter.formatNumber(t.getPublicMetrics().getLikeCount()));
        retweetLabel.setText(Formatter.formatNumber(t.getPublicMetrics().getRetweetCount()));
        replyCountLabel.setText(Formatter.formatNumber(t.getPublicMetrics().getReplyCount()));
        createdAtLabel.setText(t.getCreatedAt());
        velocityLabel.setText(Formatter.formatNumberDecimal(t.getAssociatedOutbreakMagnitude()));
        engagementShareLabel.setText(Formatter.formatNumberDecimal(t.getEngagementShare() * 100));
    }

}
