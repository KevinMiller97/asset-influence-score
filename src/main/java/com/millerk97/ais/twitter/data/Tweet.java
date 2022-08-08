package com.millerk97.ais.twitter.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.twitter.data.context.Annotation;
import com.millerk97.ais.twitter.data.entities.Entities;
import com.millerk97.ais.twitter.data.geo.Geo;
import com.millerk97.ais.twitter.data.metrics.NonPublicMetrics;
import com.millerk97.ais.twitter.data.metrics.PublicMetrics;
import com.millerk97.ais.twitter.data.user.User;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {
    @JsonProperty("id")
    String id;
    @JsonProperty("conversation_id")
    String conversationId;
    @JsonProperty("text")
    String text;
    @JsonProperty("author_id")
    String authorId;
    @JsonProperty("context_annotations")
    Annotation[] contextAnnotations;
    @JsonProperty("created_at")
    String createdAt;
    @JsonProperty("entities")
    Entities entities;
    @JsonProperty("geo")
    Geo geo;
    @JsonProperty("lang")
    String lang;
    @JsonProperty("non_public_metrics")
    NonPublicMetrics nonPublicMetrics;
    @JsonProperty("public_metrics")
    PublicMetrics publicMetrics;
    @JsonProperty("source")
    String source;
    @JsonProperty("possibly_sensitive")
    boolean possiblySensitive;
    @JsonProperty("reply_settings")
    String replySettings;
    // set manually while fetching
    User user;
}
