package com.millerk97.ais.dataframe.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TweetMap {
    private DFUser user;
    private double anomalyTweetCount = 0;
    private double regularTweetCount = 0;
    private double magnitude = 0;
    private double breakoutThreshold = 0;
    private double twitterInfluenceFactor = 0;
    private List<DFTweet> tweets = new ArrayList<>();

    public TweetMap(DFUser user, double breakoutThreshold, double twitterInfluenceFactor) {
        this.user = user;
        this.breakoutThreshold = breakoutThreshold;
        this.twitterInfluenceFactor = twitterInfluenceFactor;
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

    public double getTotalTweetCount() {
        return anomalyTweetCount + regularTweetCount;
    }

    public double getAvgMagnitude() {
        return magnitude / getTotalTweetCount();
    }

    public double getWeightedAvgMagnitude() {
        return getAvgMagnitude() * getAnomalyRatio();
    }

    public double getAvgEngagement() {
        return tweets.stream().collect(Collectors.summingDouble(DFTweet::getEngagementShare)) / getTotalTweetCount();
    }

    /**
     * incorporates middle 3 tweets
     */
    public double getMedianMagnitude() {
        return (tweets.get((tweets.size() / 2) - 1).getAssociatedOutbreakMagnitude() + tweets.get(tweets.size() / 2).getAssociatedOutbreakMagnitude() + tweets.get((tweets.size() / 2) + 1).getAssociatedOutbreakMagnitude()) / 3;
    }

    public double getMedianAttributableMagnitude() {
        return getMedianMagnitude() * ((tweets.get((tweets.size() / 2) - 1).getEngagementShare() + tweets.get(tweets.size() / 2).getEngagementShare() + tweets.get((tweets.size() / 2) + 1).getEngagementShare()) / 3);
    }

    public double getMedianAttributableMagnitudeAdjustedForTwitterInfluence() {
        return getMedianAttributableMagnitude() * twitterInfluenceFactor;
    }

    public double getAvgAttributableMagnitude() {
        return getAvgMagnitude() * getAvgEngagement();
    }

    public double getAvgAttributableMagnitudeAdjustedForTwitterInfluence() {
        return getAvgAttributableMagnitude() * twitterInfluenceFactor;
    }

    public double getAIS() {
        double aisTemp = (getMedianAttributableMagnitude() / breakoutThreshold) * 100;
        return aisTemp > 100 ? 100 : aisTemp;
    }

    public double getAnomalyRatio() {
        return getAnomalyTweetCount() / getTotalTweetCount();
    }

    public void addTweet(DFTweet tweet) {
        tweets.add(tweet);
    }
}
