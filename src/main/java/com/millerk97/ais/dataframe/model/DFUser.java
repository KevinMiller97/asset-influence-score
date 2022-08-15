package com.millerk97.ais.dataframe.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.twitter.data.user.UserPublicMetrics;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DFUser {
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
}