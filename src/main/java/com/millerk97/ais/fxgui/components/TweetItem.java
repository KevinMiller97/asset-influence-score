package com.millerk97.ais.fxgui.components;

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

    public TweetItem(String tweetId, String author, String text, int likes, int retweets, int replyCount, String createdAt, double associatedVelocity) {
        ScreenHelper.loadFXML(this, this);
        tweetIdLabel.setText(tweetId);
        authorLabel.setText(author);
        textLabel.setText(text);
        likeCountLabel.setText(String.valueOf(likes));
        retweetLabel.setText(String.valueOf(retweets));
        replyCountLabel.setText(String.valueOf(replyCount));
        createdAtLabel.setText(createdAt);
        velocityLabel.setText(String.valueOf(associatedVelocity));
    }

}
