package com.millerk97.ais.twitter.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferencedTweet {
    @JsonProperty("type")
    String type;
    @JsonProperty("id")
    String id;
}
