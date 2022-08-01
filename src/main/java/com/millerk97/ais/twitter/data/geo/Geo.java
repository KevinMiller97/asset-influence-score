package com.millerk97.ais.twitter.data.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Geo {
    @JsonProperty("coordinates")
    Coordinates coordinates;
    @JsonProperty("place_id")
    String placeId;
}
