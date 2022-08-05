package com.millerk97.ais.twitter.data.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.twitter.data.entities.Mention;
import com.millerk97.ais.twitter.data.entities.Tag;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDescription {
    @JsonProperty("mentions")
    Mention[] mentions;
    @JsonProperty("hashtags")
    Tag[] hashtags;
    @JsonProperty("cashtags")
    Tag[] cashtags;
}
