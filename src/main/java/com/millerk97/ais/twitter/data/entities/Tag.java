package com.millerk97.ais.twitter.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {
    @JsonProperty("start")
    int start;
    @JsonProperty("end")
    int end;
    @JsonProperty("tag")
    String tag;
}
