package com.millerk97.ais.twitter.api;

import com.millerk97.ais.twitter.data.APIResult;

public class TwitterApiClientImpl implements TwitterApiClient {
    static final Long DEFAULT_CONNECTION_TIMEOUT = 10L;
    static final Long DEFAULT_READ_TIMEOUT = 10L;
    static final Long DEFAULT_WRITE_TIMEOUT = 10L;

    private final TwitterApiService twitterApiService;
    private final TwitterApi twitterApi;

    public TwitterApiClientImpl() {
        this(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT);
    }

    public TwitterApiClientImpl(Long connectionTimeoutSeconds, Long readTimeoutSeconds, Long writeTimeoutSeconds) {
        this.twitterApi = new TwitterApi();
        this.twitterApiService = twitterApi.createService(
                TwitterApiService.class,
                connectionTimeoutSeconds,
                readTimeoutSeconds,
                writeTimeoutSeconds
        );
    }

    @Override
    public APIResult searchTweets(String bearerToken, String query, String startTime_ISO8601, String endTime_ISO8601) {
        return twitterApi.executeSync(twitterApiService.searchTweets(String.format("Bearer %s", bearerToken), query, startTime_ISO8601, endTime_ISO8601));
    }
}