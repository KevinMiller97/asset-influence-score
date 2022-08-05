package com.millerk97.ais.twitter.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mention {
    @JsonProperty("start")
    int start;
    @JsonProperty("end")
    int end;
    @JsonProperty("username")
    String username;
}
