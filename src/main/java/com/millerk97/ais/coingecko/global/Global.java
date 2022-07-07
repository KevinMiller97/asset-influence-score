package com.millerk97.ais.coingecko.global;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class Global {
    @JsonProperty("data")
    private GlobalData data;
}
