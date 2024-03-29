package com.millerk97.ais.twitter.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class APIResult {
    @JsonProperty("data")
    Tweet[] data;
    @JsonProperty("includes")
    IncludedUsers includedUsers;
    @JsonProperty("meta")
    Meta meta;
}
