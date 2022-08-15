package com.millerk97.ais.dataframe.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.millerk97.ais.twitter.data.Tweet;
import com.millerk97.ais.twitter.data.entities.Entities;
import com.millerk97.ais.twitter.data.metrics.PublicMetrics;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DFTweet {
    @JsonProperty("id")
    String id;
    @JsonProperty("text")
    String text;
    @JsonProperty("created_at")
    String createdAt;
    @JsonProperty("author_id")
    String authorId;
    @JsonProperty("entities")
    Entities entities;
    @JsonProperty("public_metrics")
    PublicMetrics publicMetrics;
    @JsonProperty("conversation_id")
    String conversationId;
    // set manually while fetching
    DFUser user;

    public DFTweet() {
    }

    public DFTweet(Tweet t) {
        id = t.getId();
        text = t.getText();
        createdAt = t.getCreatedAt();
        authorId = t.getAuthorId();
        entities = t.getEntities();
        publicMetrics = t.getPublicMetrics();
        conversationId = t.getConversationId();

        DFUser u = new DFUser();
        u.setId(t.getUser().getId());
        u.setCreatedAt(t.getUser().getCreatedAt());
        u.setUsername(t.getUser().getUsername());
        u.setVerified(t.getUser().isVerified());
        u.setPublicMetrics(t.getUser().getPublicMetrics());
        u.setName(t.getUser().getName());
        user = u;
    }
}