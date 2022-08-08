package com.millerk97.ais.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.twitter.api.TwitterApiClient;
import com.millerk97.ais.twitter.api.TwitterApiClientImpl;
import com.millerk97.ais.twitter.api.TwitterApiException;
import com.millerk97.ais.twitter.data.APIResult;
import com.millerk97.ais.twitter.data.Tweet;
import com.millerk97.ais.twitter.data.TweetList;
import com.millerk97.ais.twitter.data.user.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
                System.out.println(String.format("File present: %s | fetching from local storage", fileName));
                return Arrays.asList(mapper.readValue(Files.readString(Path.of(fileName)), TweetList.class).getTweets());
            } else {
                System.out.println("Fetching Tweets from API");
                List<Tweet> tweets = new ArrayList<>();
                try {
                    APIResult apiResult = api.searchTweets(query, before_iso8601, after_iso8601);
                    do {
                        // make effectively final for this iteration
                        APIResult finalApiResult = apiResult;
                        // filter out all tweets with 0 likes
                        tweets.addAll(Arrays.stream(apiResult.getData()).filter(t -> t.getPublicMetrics().getLikeCount() > 0).collect(Collectors.toList()));
                        // map the associated user to the Tweet object for convenience
                        tweets = tweets.stream().map(t -> mapUser(t, Arrays.stream(finalApiResult.getIncludedUsers().getUsers()).filter(user -> user.getId().equals(t.getAuthorId())).findAny().get())).collect(Collectors.toList());
                        // advance to next apiResult set
                        if (apiResult.getMeta().getNextToken() != null && !apiResult.getMeta().getNextToken().isBlank()) {
                            apiResult = api.searchTweets(query, before_iso8601, after_iso8601, apiResult.getMeta().getNextToken());
                        }
                    } while (apiResult.getMeta().getNextToken() != null && !apiResult.getMeta().getNextToken().isBlank() && tweets.size() < 1500);
                } catch (TwitterApiException e) {
                    try {
                        // wait 10 seconds to pull tweets again, API doesn't allow to fetch everything at once
                        System.out.println("API limit reached, waiting 10 seconds for retry");
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                TweetList result = new TweetList();
                // sort by like count
                result.setTweets(tweets.stream().sorted(Comparator.comparingInt(t -> t.getPublicMetrics().getLikeCount())).collect(Collectors.toList()).toArray(new Tweet[0]));

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

    private static Tweet mapUser(Tweet t, User u) {
        t.setUser(u);
        return t;
    }
}
