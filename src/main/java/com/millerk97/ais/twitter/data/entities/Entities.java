package com.millerk97.ais.twitter.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entities {

    @JsonProperty("cashtags")
    Tag[] cashtags;
    @JsonProperty("hashtags")
    Tag[] hashtags;
    @JsonProperty("mentions")
    Tag[] mentions;
    @JsonProperty("urls")
    URL[] urls;
}
