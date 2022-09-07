package com.millerk97.ais.dataframe.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TweetMap {
    private DFUser user;
    private double anomalyTweetCount = 0;
    private double regularTweetCount = 0;
    private double totalAttributableMagnitude = 0;
    private double breakoutThreshold;
    private List<DFTweet> tweets = new ArrayList<>();

    public TweetMap(DFUser user, double breakoutThreshold) {
        this.user = user;
        this.breakoutThreshold = breakoutThreshold;
    }

    public void incrementAnomalyTweetCount() {
        anomalyTweetCount++;
    }

    public void incrementRegularTweetCount() {
        regularTweetCount++;
    }

    public void addToTotalAttributableMagnitude(double magnitude) {
        this.totalAttributableMagnitude += magnitude;
    }

    public double getTotalTweetCount() {
        return anomalyTweetCount + regularTweetCount;
    }

    public double getAvgMagnitude() {
        return totalAttributableMagnitude / getTotalTweetCount();
    }

    public double getAvgEngagement() {
        return tweets.stream().collect(Collectors.summingDouble(DFTweet::getEngagementShare)) / getTotalTweetCount();
    }

    /**
     * incorporates middle 3 tweets
     */
    public double getMedianMagnitude() {
        List<DFTweet> sorted = tweets.stream().sorted(Comparator.comparingDouble(t -> -t.getAssociatedOutbreakMagnitude())).toList();
        return (sorted.get((sorted.size() / 2) - 1).getAssociatedOutbreakMagnitude() + sorted.get(sorted.size() / 2).getAssociatedOutbreakMagnitude() + sorted.get((sorted.size() / 2) + 1).getAssociatedOutbreakMagnitude()) / 3;
    }

    public double getMedianAttributableMagnitude() {
        List<DFTweet> sorted = tweets.stream().sorted(Comparator.comparingDouble(t -> -t.getAssociatedOutbreakMagnitude())).toList();
        return getMedianMagnitude() * ((sorted.get((sorted.size() / 2) - 1).getEngagementShare() + sorted.get(sorted.size() / 2).getEngagementShare() + sorted.get((sorted.size() / 2) + 1).getEngagementShare()) / 3);
    }

    public double getAIS() {
        double aisTemp = (getMedianAttributableMagnitude() / breakoutThreshold) * 100;
        return aisTemp > 100 ? 100 * getAnomalyRatio() : aisTemp > 0 ? aisTemp : 0;
    }

    public double getAnomalyRatio() {
        return getAnomalyTweetCount() / getTotalTweetCount();
    }

    public void addTweet(DFTweet tweet) {
        tweets.add(tweet);
    }
}
