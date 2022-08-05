package com.millerk97.ais.twitter.data.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonProperty("profile_image_url")
    String profileImageUrl;
    @JsonProperty("created_at")
    String createdAt;
    @JsonProperty("username")
    String username;
    @JsonProperty("public_metrics")
    UserPublicMetrics publicMetrics;
    @JsonProperty("verified")
    boolean verified;
    @JsonProperty("id")
    String id;
    @JsonProperty("name")
    String name;
    @JsonProperty("entities")
    UserEntitities entities;
    @JsonProperty("pinned_tweet_id")
    String pinnedTweetId;
    @JsonProperty("protected")
    boolean isProtected;
    @JsonProperty("url")
    String url;
    @JsonProperty("description")
    String description;
}
