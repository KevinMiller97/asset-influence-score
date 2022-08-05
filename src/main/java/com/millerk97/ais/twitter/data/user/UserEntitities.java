package com.millerk97.ais.twitter.data.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntitities {
    @JsonProperty("url")
    UserURL url;
    @JsonProperty("description")
    UserDescription description;

}
