package com.millerk97.ais.coingecko.domain.MarketChart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry {
    String[] values;
}
