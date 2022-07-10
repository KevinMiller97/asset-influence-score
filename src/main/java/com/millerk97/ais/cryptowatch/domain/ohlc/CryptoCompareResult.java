package com.millerk97.ais.cryptowatch.domain.ohlc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCompareResult {
    @JsonProperty("Response")
    String response;
    @JsonProperty("Message")
    String message;
    @JsonProperty("HasWarning")
    boolean hasWarning;
    @JsonProperty("Type")
    int type;
    @JsonProperty("Data")
    CryptoCompareData data;
}
