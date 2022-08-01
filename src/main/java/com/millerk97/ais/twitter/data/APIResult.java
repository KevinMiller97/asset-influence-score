package com.millerk97.ais.twitter.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class APIResult {
    @JsonProperty("data")
    Tweet[] data;
    @JsonProperty("meta")
    Meta meta;
}
