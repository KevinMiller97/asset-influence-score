package com.millerk97.ais.twitter.data.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicMetrics {
    @JsonProperty("like_count")
    int likeCount;
    @JsonProperty("reply_count")
    int replyCount;
    @JsonProperty("retweet_count")
    int retweetCount;
    @JsonProperty("quote_count")
    int quoteCount;
}
