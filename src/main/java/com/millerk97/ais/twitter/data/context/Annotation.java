package com.millerk97.ais.twitter.data.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Annotation {
    @JsonProperty("domain")
    Detail domain;
    @JsonProperty("entity")
    Detail entity;
}
