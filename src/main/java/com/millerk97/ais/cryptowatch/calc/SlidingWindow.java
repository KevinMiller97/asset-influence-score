package com.millerk97.ais.cryptowatch.calc;

import com.millerk97.ais.cryptowatch.domain.ohlc.OHLC;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation inspired by Alostad et. al.
 * <p>
 * H. Alostad and H. Davulcu, “Directional prediction of stock prices using breaking news on Twitter,” WEB Intell., vol. 15, no. 1, pp. 1–17, 2017, doi: 10.3233/WEB-170349.
 */

public class SlidingWindow {

    private final List<OHLC> candles;
    private final int windowSize;
    private final double breakoutThreshold;

    private List<OHLC> currentCandles;
    private int index;

    public SlidingWindow(List<OHLC> candles, int windowSize, double breakoutThreshold) {
        this.candles = candles;
        this.windowSize = windowSize;
        this.breakoutThreshold = breakoutThreshold;
        this.index = windowSize;
        this.currentCandles = candles.stream().limit(windowSize).collect(Collectors.toList());
    }

    public void resetWindow() {
        this.currentCandles = candles.stream().limit(windowSize).collect(Collectors.toList());
    }

    public List<OHLC> findAnomalies() {
        return findAnomalies(0, candles.size());
    }

    public List<OHLC> findAnomalies(int fromIndex, int untilIndex) {
        List<OHLC> anomalies = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        for (int i = fromIndex; i < untilIndex - 1; i++) {
            if (calc(candles.get(i)) > breakoutThreshold * calculateStandardDeviation()) {

                System.out.println(String.format("On: %s | Threshold * SD: %.9f | This candle: %.9f", formatter.format(new Date((candles.get(i).getCloseTime() - 86400) * 1000)), breakoutThreshold * calculateStandardDeviation(), calc(candles.get(i))));
                anomalies.add(candles.get(i));
            }
            advanceWindow();
        }
        return anomalies;
    }

    /**
     * an experimental function which should be used to experiment with different calculations on OHLC candles
     */
    private double calc(OHLC ohlc) {
        return ohlc.getHighPrice() - ohlc.getLowPrice();
    }

    public void advanceWindow() {
        index++;
        this.currentCandles = IntStream.range(index - windowSize, index < candles.size() ? index : candles.size()).mapToObj(i -> candles.get(i)).collect(Collectors.toList());
    }

    public double calculateMeanFluctuation() {
        // discover volatility by comparing candle high to candle low
        return currentCandles.stream().mapToDouble(ohlc -> calc(ohlc)).sum() / windowSize;
    }

    public double calculateStandardDeviation() {
        return Math.sqrt(currentCandles.stream().mapToDouble(ohlc -> (Math.pow(calc(ohlc) - calculateMeanFluctuation(), 2))).sum() / windowSize);
    }

}
