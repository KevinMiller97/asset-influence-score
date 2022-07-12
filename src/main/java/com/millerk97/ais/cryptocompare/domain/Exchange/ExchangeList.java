package com.millerk97.ais.cryptocompare.domain.Exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeList {
    @JsonProperty("result")
    List<Exchange> exchanges;
}
