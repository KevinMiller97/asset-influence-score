package com.millerk97.ais.cryptocompare.calc;

import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnomalyDay {
    private final List<OHLC> candles;
    private final double breakoutThreshold;

    public AnomalyDay(List<OHLC> candles, double breakoutThreshold) {
        this.candles = candles;
        this.breakoutThreshold = breakoutThreshold;
    }

    public List<OHLC> findAnomalies(boolean print) {
        return findAnomalies(0, candles.size(), print);
    }

    /**
     * returns a list of OHLC that have {breakoutThreshold} * the mean of price movement
     */
    public List<OHLC> findAnomalies(int fromIndex, int untilIndex, boolean print) {
        List<OHLC> anomalies = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        for (int i = fromIndex; i < untilIndex - 1; i++) {
            if (print) {
                long timestamp = candles.get(i).getTime();
                System.out.print(String.format("hourly | open: %.5f | close: %.5f | On: %s (%s) | Threshold * SD: %.9f | This candle: %.9f", candles.get(i).getOpen(), candles.get(i).getClose(), formatter.format(new Date(timestamp * 1000)), timestamp, breakoutThreshold * calculateMeanFluctuation(), calc(candles.get(i))));
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
        if (print) {
            System.out.println("____________________________________________");
        }
        return anomalies;
    }

    /**
     * an experimental function which should be used to experiment with different calculations on OHLC candles
     */
    private double calc(OHLC ohlc) {
        return (ohlc.getHigh() - ohlc.getLow()) * ohlc.getVolumeTo() / 10000;
    }

    public double calculateMeanFluctuation() {
        // discover volatility by comparing candle high to candle low
        return candles.stream().mapToDouble(ohlc -> calc(ohlc)).sum() / candles.size();
    }
}
