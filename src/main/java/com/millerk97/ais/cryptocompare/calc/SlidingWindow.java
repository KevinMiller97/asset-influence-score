package com.millerk97.ais.cryptocompare.calc;

import com.millerk97.ais.controller.AISToolkit;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLCStatistics;
import javafx.util.Pair;

import java.util.ArrayList;
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

    private List<OHLC> currentCandles;
    private int index;

    public SlidingWindow(List<OHLC> candles, int windowSize) {
        this.candles = candles;
        this.windowSize = windowSize;
        this.index = windowSize;
        this.currentCandles = candles.stream().limit(windowSize).collect(Collectors.toList());
    }

    public void resetWindow() {
        this.currentCandles = candles.stream().limit(windowSize).collect(Collectors.toList());
    }

    public List<Pair<OHLC, OHLCStatistics>> getStatistics() {
        return getStatistics(0);
    }

    public List<Pair<OHLC, OHLCStatistics>> getStatistics(long earliestTimestamp) {
        List<Pair<OHLC, OHLCStatistics>> statistics = new ArrayList<>();
        // we start at windowSize because we need "historical" data for day 1
        for (int i = windowSize; i < candles.size() - 1; i++) {
            if (candles.get(i).getTime() > earliestTimestamp) {
                OHLCStatistics stats = new OHLCStatistics();
                stats.setWindowSize(windowSize);
                stats.setIndex(i);
                stats.setMeanFluctuation(calculateMeanFluctuation());
                stats.setMeanVariance(calculateMeanVariance());
                stats.setMeanVolume(calculateMeanVolume());
                stats.setPreviousClosePrice(candles.get(i - 1).getClose());
                statistics.add(new Pair<>(candles.get(i), stats));
            }
            advanceWindow();
        }
        return statistics;
    }

    public void advanceWindow() {
        index++;
        this.currentCandles = IntStream.range(index - windowSize, index < candles.size() ? index : candles.size()).mapToObj(i -> candles.get(i)).collect(Collectors.toList());
    }

    public double calculateMeanFluctuation() {
        // discover volatility by comparing candle high to candle low
        return currentCandles.stream().mapToDouble(ohlc -> AISToolkit.calculateCandleVelocity(ohlc)).sum() / windowSize;
    }

    public double calculateMeanVariance() {
        return Math.sqrt(currentCandles.stream().mapToDouble(ohlc -> (Math.pow(AISToolkit.calculateCandleVelocity(ohlc) - calculateMeanFluctuation(), 2))).sum() / windowSize);
    }

    public double calculateMeanVolume() {
        return currentCandles.stream().mapToDouble(OHLC::getVolumeTo).sum() / windowSize;
    }

}
