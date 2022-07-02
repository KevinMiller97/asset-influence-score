package com.millerk97.ais.twitter;

import twitter4j.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Twitterer {
    private final Twitter twitter;
    private final PrintStream consolePrint;
    private final List<Status> statuses;

    public Twitterer(PrintStream console) {
        // Makes an instance of Twitter - this is re-useable and thread safe.
        // Connects to Twitter and performs authorizations.
        twitter = TwitterFactory.getSingleton();
        consolePrint = console;
        statuses = new ArrayList<Status>();
    }

    public void tweetOut(String message) throws TwitterException, IOException {

    }


    @SuppressWarnings("unchecked")
    public void queryHandle(String handle) throws TwitterException, IOException {
        statuses.clear();
        fetchTweets(handle);
        int counter = statuses.size();
        while (counter-- > 0) {
            System.out.println("Tweet #" + counter + ": " + statuses.get(counter).getText());
        }
    }

    private void fetchTweets(String handle) throws TwitterException, IOException {
        Paging page = new Paging(1, 10);
        int p = 1;
        Query query = new Query("dogecoin");
        query.setResultType(Query.ResultType.popular);
        QueryResult result;
        do {
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            for (Status tweet : tweets) {
                System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
            }
        } while ((query = result.nextQuery()) != null);
    }

    public void saQuery(String searchTerm) {

    }

}

