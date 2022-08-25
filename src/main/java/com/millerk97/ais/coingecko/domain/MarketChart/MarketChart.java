package com.millerk97.ais.coingecko.domain.MarketChart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketChart {
    @JsonProperty("prices")
    private String[][] prices;
    @JsonProperty("market_caps")
    private String[][] mcaps;
}
