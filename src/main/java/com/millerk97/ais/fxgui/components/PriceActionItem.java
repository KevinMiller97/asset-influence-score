package com.millerk97.ais.fxgui.components;

import com.millerk97.ais.controller.AISToolkit;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLCStatistics;
import com.millerk97.ais.util.Formatter;
import com.millerk97.ais.util.ScreenHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

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
    private Label priceMoveInPercent;
    @FXML
    private Label meanVolumeLabel;
    @FXML
    private Label meanStandardDeviation;
    @FXML
    private Label varianceLabel;
    @FXML
    private Label candleVelocityLabel;
    @FXML
    private Label velocityExternalFactorsLabel;
    @FXML
    private Label isLabel;
    @FXML
    private Label anomalyThresholdLabel;
    @FXML
    private Label magnitudeLabel;
    @FXML
    private Label bitcoinMagnitudeLabel;
    @FXML
    private Label magExternalLabel;
    @FXML
    private Button showTweetsButton;

    public PriceActionItem(Pair<OHLC, OHLCStatistics> pair, String timeframe, int breakoutThresholdFactor, boolean anomaly, boolean showTweetsButtonVisible, double currentInfluenceabilityScore, Runnable showTweetsAction) {
        ScreenHelper.loadFXML(this, this);
        OHLC ohlc = pair.getKey();
        OHLCStatistics stat = pair.getValue();
        timeframeLabel.setText(timeframe);
        candleOpenLabel.setText(Formatter.formatNumberDecimal(ohlc.getOpen()));
        candleHighLabel.setText(Formatter.formatNumberDecimal(ohlc.getHigh()));
        candleLowLabel.setText(Formatter.formatNumberDecimal(ohlc.getLow()));
        candleCloseLabel.setText(Formatter.formatNumberDecimal(ohlc.getClose()));
        candleVolumeLabel.setText(Formatter.formatNumberDecimal(ohlc.getVolumeTo()));
        priceMoveInPercent.setText(Formatter.formatNumberDecimal((ohlc.getClose() / stat.getPreviousClosePrice() - 1) * 100));
        meanVolumeLabel.setText(Formatter.formatNumberDecimal(stat.getMeanVolume()));
        meanStandardDeviation.setText(Formatter.formatNumberDecimal(stat.getMeanFluctuation()));
        varianceLabel.setText(Formatter.formatNumberDecimal(stat.getMeanVariance()));
        candleVelocityLabel.setText(Formatter.formatNumberDecimal(AISToolkit.calculateCandleVelocity(ohlc)));
        anomalyThresholdLabel.setText(Formatter.formatNumberDecimal(breakoutThresholdFactor * stat.getMeanFluctuation()));
        magnitudeLabel.setText(Formatter.formatNumberDecimal(AISToolkit.calculateOutbreakMagnitude(pair)));
        isLabel.setText(Formatter.formatNumberDecimal(currentInfluenceabilityScore));
        bitcoinMagnitudeLabel.setText(Formatter.formatNumberDecimal(AISToolkit.getAssociatedBitcoinMagnitude(ohlc.getTime())));
        double magExternal = AISToolkit.calculateOutbreakMagnitudeAttributableToExternalFactors(pair);
        magExternalLabel.setText(Formatter.formatNumberDecimal(magExternal > 0 ? magExternal : 0));
        showTweetsButton.setOnAction(action -> showTweetsAction.run());
        showTweetsButton.setVisible(showTweetsButtonVisible);
        if (anomaly) {
            root.setStyle(root.getStyle() + ";-fx-background-color: ORANGE;");
        }
    }

}
