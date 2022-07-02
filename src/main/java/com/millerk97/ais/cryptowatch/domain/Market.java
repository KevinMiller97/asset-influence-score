package com.millerk97.ais.cryptowatch.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Market {
    @JsonProperty("id")
    String id;
    @JsonProperty("exchange")
    String exchange;
    @JsonProperty("pair")
    String pair;
    @JsonProperty("active")
    boolean active;
    @JsonProperty("route")
    String route;
}
