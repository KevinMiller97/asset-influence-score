package com.millerk97.ais.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.twitter.api.TwitterApiClient;
import com.millerk97.ais.twitter.api.TwitterApiClientImpl;
import com.millerk97.ais.twitter.data.APIResult;
import com.millerk97.ais.twitter.data.Tweet;
import com.millerk97.ais.twitter.data.TweetList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TweetFetcher {

    private static final String PREFIX = "src/main/resources/com/millerk97/tweets/";
    private static final String FILE_TEMPLATE = "%s_%s_%s.json";

    private static final TwitterApiClient api = new TwitterApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Tweet> fetchTweets(String query, String before_iso8601, String after_iso8601) {

        String fileName = PREFIX + String.format(FILE_TEMPLATE, query, before_iso8601.substring(0, 13), after_iso8601.substring(0, 13));
        
        try {
            if (new File(fileName).exists() && !Files.readString(Path.of(fileName)).isBlank()) {
                return Arrays.asList(mapper.readValue(Files.readString(Path.of(fileName)), TweetList.class).getTweets());
            } else {
                System.out.println("Fetching Tweets from API");

                List<Tweet> tweets = new ArrayList<>();

                APIResult apiResult = api.searchTweets(query, before_iso8601, after_iso8601);
                do {
                    // filter out all tweets with 0 likes
                    tweets.addAll(Arrays.stream(apiResult.getData()).filter(t -> t.getPublicMetrics().getLikeCount() > 0).collect(Collectors.toList()));
                    System.out.println("Tweet size: " + tweets.size());
                    System.out.println("next: " + apiResult.getMeta().getNextToken());
                    // advance to next apiResult set
                    if (apiResult.getMeta().getNextToken() != null && !apiResult.getMeta().getNextToken().isBlank()) {
                        apiResult = api.searchTweets(query, before_iso8601, after_iso8601, apiResult.getMeta().getNextToken());
                    }
                }
                while (apiResult.getMeta().getNextToken() != null && !apiResult.getMeta().getNextToken().isBlank() && tweets.size() < 200);

                TweetList result = new TweetList();
                result.setTweets(tweets.toArray(new Tweet[0]));

                FileWriter fWriter = new FileWriter(fileName);
                fWriter.write(mapper.writeValueAsString(result));
                fWriter.flush();
                fWriter.close();
                return tweets;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
