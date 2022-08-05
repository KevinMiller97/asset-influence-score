package com.millerk97.ais.twitter.api;

import com.millerk97.ais.twitter.data.APIResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface TwitterApiService {

    @Headers({
            "Authorization: Bearer AAAAAAAAAAAAAAAAAAAAAKNLfAEAAAAAgIWEHlNfQ%2Fk5qB6kiXHjhpdFR7Y%3DJhcm50NdhEc4v1j52DAXEBQvjEQLkhRBWLF5ehsY8uOP8E7vht"
    })
    @GET("tweets/search/all?tweet.fields=attachments,author_id,context_annotations,conversation_id,created_at,entities,geo,id,in_reply_to_user_id,lang,possibly_sensitive,public_metrics,referenced_tweets,reply_settings,source,text,withheld&expansions=attachments.media_keys,attachments.poll_ids,author_id,entities.mentions.username,geo.place_id,in_reply_to_user_id,referenced_tweets.id,referenced_tweets.id.author_id&media.fields=alt_text,duration_ms,height,media_key,preview_image_url,public_metrics,type,url,variants,width&user.fields=created_at,description,entities,id,location,name,pinned_tweet_id,profile_image_url,protected,public_metrics,url,username,verified,withheld&place.fields=contained_within,country,country_code,full_name,geo,id,name,place_type&max_results=100&sort_order=relevancy")
    Call<APIResult> searchTweets(@Query("query") String query, @Query("start_time") String startTime_ISO8601, @Query("end_time") String endTime_ISO8601);

    @Headers({
            "Authorization: Bearer AAAAAAAAAAAAAAAAAAAAAKNLfAEAAAAAgIWEHlNfQ%2Fk5qB6kiXHjhpdFR7Y%3DJhcm50NdhEc4v1j52DAXEBQvjEQLkhRBWLF5ehsY8uOP8E7vht"
    })
    @GET("tweets/search/all?tweet.fields=attachments,author_id,context_annotations,conversation_id,created_at,entities,geo,id,in_reply_to_user_id,lang,possibly_sensitive,public_metrics,referenced_tweets,reply_settings,source,text,withheld&expansions=attachments.media_keys,attachments.poll_ids,author_id,entities.mentions.username,geo.place_id,in_reply_to_user_id,referenced_tweets.id,referenced_tweets.id.author_id&media.fields=alt_text,duration_ms,height,media_key,preview_image_url,public_metrics,type,url,variants,width&user.fields=created_at,description,entities,id,location,name,pinned_tweet_id,profile_image_url,protected,public_metrics,url,username,verified,withheld&place.fields=contained_within,country,country_code,full_name,geo,id,name,place_type&max_results=100&sort_order=relevancy")
    Call<APIResult> searchTweets(@Query("query") String query, @Query("start_time") String startTime_ISO8601, @Query("end_time") String endTime_ISO8601, @Query("next_token") String next_token);

}