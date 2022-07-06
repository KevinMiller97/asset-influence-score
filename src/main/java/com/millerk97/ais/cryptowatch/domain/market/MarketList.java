package com.millerk97.ais.cryptowatch.domain.market;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketList {
    @JsonProperty("result")
    List<Market> markets;
}
