package com.millerk97.ais.cryptocompare.domain.ohlc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OHLCList {
    @JsonProperty("hours")
    OHLC[] ohlcData;
}
