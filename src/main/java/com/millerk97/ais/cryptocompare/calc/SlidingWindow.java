package com.millerk97.ais.cryptocompare.calc;

import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;

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
    private final String timeframe;

    private List<OHLC> currentCandles;
    private int index;

    public SlidingWindow(List<OHLC> candles, int windowSize, double breakoutThreshold, String timeframe) {
        this.candles = candles;
        this.windowSize = windowSize;
        this.breakoutThreshold = breakoutThreshold;
        this.index = windowSize;
        this.timeframe = timeframe;
        this.currentCandles = candles.stream().limit(windowSize).collect(Collectors.toList());
    }

    public void resetWindow() {
        this.currentCandles = candles.stream().limit(windowSize).collect(Collectors.toList());
    }

    public List<OHLC> findAnomalies(boolean print) {
        return findAnomalies(0, print);
    }

    public List<OHLC> findAnomalies(long earliestTimestamp, boolean print) {
        List<OHLC> anomalies = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        // we start at windowSize because we need "historical" data for day 1
        for (int i = windowSize; i < candles.size() - 1; i++) {
            if (candles.get(i).getTime() > earliestTimestamp) {
                if (print) {
                    long timestamp = candles.get(i).getTime();
                    System.out.print(String.format("TF: daily | On: %s (%s) | Threshold: %15.9f | This: %15.9f |", formatter.format(new Date(timestamp * 1000)), timestamp, breakoutThreshold * calculateMeanFluctuation(), calc(candles.get(i))));
                }
                if (calc(candles.get(i)) > breakoutThreshold * calculateMeanFluctuation()) {
                    anomalies.add(candles.get(i));
                    if (print) {
                        System.out.print(" XXX\n");
                    }
                } else {
                    if (print) {
                        System.out.print("\n");
                    }
                }
            }
            advanceWindow();
        }
        // System.out.println("all anomalies found");
        return anomalies;
    }

    /**
     * an experimental function which should be used to experiment with different calculations on OHLC candles
     */
    private double calc(OHLC ohlc) {
        return (ohlc.getHigh() - ohlc.getLow()) * ohlc.getVolumeTo() / 10000;
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
