package com.millerk97.ais.cryptowatch.domain.ohlc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.cryptowatch.constant.ResponseIndex;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OHLC {
    @JsonProperty("closeTime")
    long closeTime;
    @JsonProperty("openPrice")
    double openPrice;
    @JsonProperty("highPrice")
    double highPrice;
    @JsonProperty("lowPrice")
    double lowPrice;
    @JsonProperty("closePrice")
    double closePrice;
    @JsonProperty("volume")
    long volume;
    @JsonProperty("quoteVolume")
    long quoteVolume;

    public OHLC() {
    }

    public static OHLC of(double[] data) {
        OHLC ohlc = new OHLC();
        ohlc.setCloseTime((long) data[ResponseIndex.CLOSE_TIME]);
        ohlc.setOpenPrice(data[ResponseIndex.OPEN_PRICE]);
        ohlc.setHighPrice(data[ResponseIndex.HIGH_PRICE]);
        ohlc.setLowPrice(data[ResponseIndex.LOW_PRICE]);
        ohlc.setClosePrice(data[ResponseIndex.CLOSE_PRICE]);
        ohlc.setVolume((long) data[ResponseIndex.VOLUME]);
        ohlc.setQuoteVolume((long) data[ResponseIndex.QUOTE_VOLUME]);
        return ohlc;
    }

    public static OHLC of(long closeTime, double openPrice, double highPrice, double lowPrice, double closePrice, long volume, long quoteVolume) {
        OHLC ohlc = new OHLC();
        ohlc.closeTime = closeTime;
        ohlc.openPrice = openPrice;
        ohlc.highPrice = highPrice;
        ohlc.lowPrice = lowPrice;
        ohlc.closePrice = closePrice;
        ohlc.volume = volume;
        ohlc.quoteVolume = quoteVolume;
        return ohlc;
    }
}
