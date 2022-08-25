package com.millerk97.ais.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.controller.FlowController;
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
import java.util.List;
import java.util.stream.Collectors;

public class TweetFetcher {

    private static final String PREFIX = "src/main/resources/com/millerk97/tweets/%s/";
    private static final String FILE_TEMPLATE = "/%s_%s_%s.json";

    private static final TwitterApiClient api = new TwitterApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void moveAndRename() {
        File dirold = new File("src/main/resources/com/millerk97/tweets/v2/");
        for (File f : dirold.listFiles()) {
            if (f.isFile()) {
                try {
                    FileWriter fWriter = new FileWriter("src/main/resources/com/millerk97/tweets/v2/dogecoin/" + f.getName().substring(8));
                    fWriter.write(mapper.writeValueAsString(mapper.readValue(Files.readString(Path.of(f.getPath())), TweetList.class)));
                    fWriter.flush();
                    fWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<Tweet> fetchTweets(String cryptocurrency, String query, String from_iso8601, String to_iso8601) throws TwitterApiException {

        File dir = new File(String.format(PREFIX, cryptocurrency.toLowerCase()));

        String fileName = dir + String.format(FILE_TEMPLATE, query, from_iso8601.substring(0, 13), to_iso8601.substring(0, 13));

        dir.mkdirs();

        try {
            if (new File(fileName).exists() && !Files.readString(Path.of(fileName)).isBlank()) {
                return Arrays.asList(mapper.readValue(Files.readString(Path.of(fileName)), TweetList.class).getTweets());
            } else {
                FlowController.log(String.format("Fetching Tweets from API: \"%s\" | From: %s   To: %s", query, from_iso8601, to_iso8601));
                List<Tweet> tweets = new ArrayList<>();
                APIResult apiResult = api.searchTweets(query, from_iso8601, to_iso8601);
                // make effectively final for this iteration
                APIResult finalApiResult = apiResult;
                // filter out all tweets with 0 likes
                tweets.addAll(Arrays.stream(apiResult.getData()).filter(t -> t.getPublicMetrics().getLikeCount() > 0).collect(Collectors.toList()));
                // map the associated user to the Tweet object for convenience

                tweets = tweets.stream().filter(t -> isUserPresent(t, finalApiResult.getIncludedUsers().getUsers())).map(t -> mapUser(t, Arrays.stream(finalApiResult.getIncludedUsers().getUsers()).filter(user -> user.getId().equals(t.getAuthorId())).findAny().get())).collect(Collectors.toList());

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

    private static boolean isUserPresent(Tweet t, User[] users) {
        return Arrays.stream(users).filter(user -> user.getId().equals(t.getAuthorId())).findAny().isPresent();
    }

    private static Tweet mapUser(Tweet t, User u) {
        t.setUser(u);
        return t;
    }
}
