package com.millerk97.ais.twitter.data.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPublicMetrics {
    @JsonProperty("followers_count")
    int followerCount;
    @JsonProperty("following_count")
    int followingCount;
    @JsonProperty("tweet_count")
    int tweetCount;
    @JsonProperty("listed_count")
    int listedCount;
}
