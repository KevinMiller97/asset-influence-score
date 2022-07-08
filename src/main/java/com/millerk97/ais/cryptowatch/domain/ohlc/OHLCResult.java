package com.millerk97.ais.cryptowatch.domain.ohlc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OHLCResult {
    @JsonProperty("result")
    Map<String, double[][]> result;
}
