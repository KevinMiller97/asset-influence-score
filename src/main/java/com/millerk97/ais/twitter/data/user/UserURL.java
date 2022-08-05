package com.millerk97.ais.twitter.data.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.twitter.data.entities.URL;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserURL {
    @JsonProperty("urls")
    URL[] urls;
}
