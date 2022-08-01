package com.millerk97.ais.twitter.data.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coordinates {
    @JsonProperty("type")
    String type;
    @JsonProperty("coordinates")
    float[] coordinates;
}
