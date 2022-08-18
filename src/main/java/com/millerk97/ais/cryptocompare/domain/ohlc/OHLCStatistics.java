package com.millerk97.ais.cryptocompare.domain.ohlc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OHLCStatistics {
    @JsonProperty("window_size")
    double windowSize;
    @JsonProperty("index")
    int index;
    @JsonProperty("mean_fluctuation")
    double meanFluctuation;
    @JsonProperty("mean_standard_deviation")
    double meanStandardDeviation;
}
