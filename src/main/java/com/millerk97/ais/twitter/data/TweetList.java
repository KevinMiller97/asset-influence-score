package com.millerk97.ais.twitter.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TweetList {
    @JsonProperty("tweets")
    Tweet[] tweets;
}
