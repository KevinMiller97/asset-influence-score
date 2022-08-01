package com.millerk97.ais.twitter.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterApiError {
    @JsonProperty("code")
    private int code;
    @JsonProperty("error")
    private String message;

}