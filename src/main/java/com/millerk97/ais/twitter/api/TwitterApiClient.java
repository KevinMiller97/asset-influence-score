package com.millerk97.ais.twitter.api;

import com.millerk97.ais.twitter.data.Tweet;
import retrofit2.http.Query;

import java.util.List;

public interface TwitterApiClient {

    List<Tweet> searchTweets(@Query("query") String query, @Query("start_time") String startTime_ISO8601, @Query("end_time") String endTime_ISO8601);

    List<Tweet> searchTweets(@Query("query") String query);
}
