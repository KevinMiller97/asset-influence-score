package com.millerk97.ais.twitter.api;

import com.millerk97.ais.twitter.data.APIResult;
import retrofit2.http.Query;

public interface TwitterApiClient {

    APIResult searchTweets(@Query("query") String query, @Query("start_time") String startTime_ISO8601, @Query("end_time") String endTime_ISO8601);

    APIResult searchTweets(@Query("query") String query, @Query("start_time") String startTime_ISO8601, @Query("end_time") String endTime_ISO8601, @Query("next_token") String next_token);
}
