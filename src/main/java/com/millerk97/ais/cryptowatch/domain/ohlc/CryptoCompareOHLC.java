package com.millerk97.ais.cryptowatch.domain.ohlc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCompareOHLC {
    @JsonProperty("time")
    long time;
    @JsonProperty("high")
    double high;
    @JsonProperty("low")
    double low;
    @JsonProperty("open")
    double open;
    @JsonProperty("volumefrom")
    double volumeFrom;
    @JsonProperty("volumeto")
    double volumeTo;
    @JsonProperty("close")
    double close;
}
