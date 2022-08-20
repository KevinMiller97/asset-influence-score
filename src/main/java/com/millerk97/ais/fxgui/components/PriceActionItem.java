package com.millerk97.ais.fxgui.components;

import com.millerk97.ais.util.ScreenHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class PriceActionItem extends GridPane {

    @FXML
    private GridPane root;
    @FXML
    private Label timeframeLabel;
    @FXML
    private Label candleOpenLabel;
    @FXML
    private Label candleHighLabel;
    @FXML
    private Label candleLowLabel;
    @FXML
    private Label candleCloseLabel;
    @FXML
    private Label candleVolumeLabel;
    @FXML
    private Label candleVelocityLabel;
    @FXML
    private Label anomalyThresholdLabel;
    @FXML
    private Label magnitudeLabel;

    public PriceActionItem(String timeframe, double open, double close, double high, double low, double volume, double velocity, double breakoutThreshold, double magnitude, boolean anomaly) {
        ScreenHelper.loadFXML(this, this);
        timeframeLabel.setText(timeframe);
        candleOpenLabel.setText(String.format("%.4f", open));
        candleHighLabel.setText(String.format("%.4f", close));
        candleLowLabel.setText(String.format("%.4f", high));
        candleCloseLabel.setText(String.format("%.4f", low));
        candleVolumeLabel.setText(String.format("%.4f", volume));
        candleVelocityLabel.setText(String.format("%.4f", velocity));
        anomalyThresholdLabel.setText(String.format("%.4f", breakoutThreshold));
        magnitudeLabel.setText(String.format("%.4f", magnitude));
        if (anomaly) {
            root.setStyle(root.getStyle() + ";-fx-background-color: ORANGE;");
        }
    }

}
