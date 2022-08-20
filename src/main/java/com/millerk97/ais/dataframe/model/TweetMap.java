package com.millerk97.ais.dataframe.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TweetMap {
    private DFUser user;
    private double anomalyTweetCount = 0;
    private double regularTweetCount = 0;
    private double magnitude = 0;
    private List<DFTweet> tweets = new ArrayList<>();

    public TweetMap(DFUser user) {
        this.user = user;
    }

    public void incrementAnomalyTweetCount() {
        anomalyTweetCount++;
    }

    public void incrementRegularTweetCount() {
        regularTweetCount++;
    }

    public void addToMagnitude(double magnitude) {
        this.magnitude += magnitude;
    }

    public double computeAnomalyRatio() {
        return anomalyTweetCount / (anomalyTweetCount + regularTweetCount);
    }

    public double getTotalTweetCount() {
        return anomalyTweetCount + regularTweetCount;
    }

    public double getAvgMagnitude() {
        return magnitude / getTotalTweetCount();
    }

    public double getWeightedAvgMagnitude() {
        return getAvgMagnitude() * computeAnomalyRatio();
    }

    public void addTweet(DFTweet tweet) {
        tweets.add(tweet);
    }
}
