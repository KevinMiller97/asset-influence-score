package com.millerk97.ais.twitter.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Annotation {
    @JsonProperty("start")
    String start;
    @JsonProperty("end")
    String end;
    @JsonProperty("probability")
    double probability;
    @JsonProperty("type")
    String type;
    @JsonProperty("normalized_text")
    String normalizedText;
}
