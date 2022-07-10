package com.millerk97.ais.cryptowatch.domain.ohlc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCompareData {
    @JsonProperty("Aggregated")
    boolean aggregated;
    @JsonProperty("TimeFrom")
    long timeFrom;
    @JsonProperty("TimeTo")
    long timeTo;
    @JsonProperty("Data")
    CryptoCompareOHLC[] data;
}
