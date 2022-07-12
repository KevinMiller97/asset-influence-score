package com.millerk97.ais.cryptocompare.domain.ohlc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.cryptocompare.constant.ResponseIndex;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OHLC {
    @JsonProperty("time")
    long time;
    @JsonProperty("high")
    double high;
    @JsonProperty("low")
    double low;
    @JsonProperty("open")
    double open;
    @JsonProperty("close")
    double close;
    @JsonProperty("volumefrom")
    double volumeFrom;
    @JsonProperty("volumeto")
    double volumeTo;

    public static OHLC of(double[] data) {
        OHLC ohlc = new OHLC();
        ohlc.time = (long) data[ResponseIndex.TIME];
        ohlc.high = data[ResponseIndex.HIGH];
        ohlc.low = data[ResponseIndex.LOW];
        ohlc.open = data[ResponseIndex.OPEN];
        ohlc.close = (long) data[ResponseIndex.CLOSE];
        ohlc.volumeFrom = data[ResponseIndex.VOLUME_FROM];
        ohlc.volumeTo = data[ResponseIndex.VOLUME_TO];
        return ohlc;
    }

    public static OHLC of(long closeTime, double openPrice, double highPrice, double lowPrice, double closePrice, long volumeFrom, long volumeTo) {
        OHLC ohlc = new OHLC();
        ohlc.time = closeTime;
        ohlc.high = highPrice;
        ohlc.low = lowPrice;
        ohlc.open = openPrice;
        ohlc.close = closePrice;
        ohlc.volumeFrom = volumeFrom;
        ohlc.volumeTo = volumeTo;
        return ohlc;
    }

}
