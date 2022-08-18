package com.millerk97.ais.dataframe.model;

import lombok.Data;

@Data
public class TweetMap {
    private final String authorId;
    private double anomalyTweetCount;
    private double regularTweetCount;

    public TweetMap(String authorId, double anomalyTweets, double regularTweets) {
        this.authorId = authorId;
        this.anomalyTweetCount = anomalyTweets;
        this.regularTweetCount = regularTweets;
    }

    public double computeAnomalyRatio() {
        return anomalyTweetCount / (anomalyTweetCount + regularTweetCount);
    }

    public double getTotalTweetCount() {
        return anomalyTweetCount + regularTweetCount;
    }
}
