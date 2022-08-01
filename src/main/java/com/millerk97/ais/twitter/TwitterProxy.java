package com.millerk97.ais.twitter;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import twitter4j.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class TwitterProxy {
    private final Twitter twitter;
    private final List<Status> statuses;

    public TwitterProxy() {
        twitter = TwitterFactory.getSingleton();
        statuses = new ArrayList<Status>();
    }

    /*
curl "https://api.twitter.com/2/tweets/search/all?start_time=2021-04-16T00:00:00.000Z&end_time=2021-04-17T00:00:00.000Z&max_results=10&pagination_token=1&tweet.fields=attachments,author_id,context_annotations,conversation_id,created_at,entities,geo,id,in_reply_to_user_id,lang,possibly_sensitive,public_metrics,referenced_tweets,reply_settings,source,text,withheld&media.fields=alt_text,media_key,public_metrics,url&user.fields=created_at,description,id,location,name,pinned_tweet_id,public_metrics,username,verified&place.fields=country,country_code,geo,name" -H "Authorization: Bearer $BEARER_TOKEN"
     */

    private static ColumnPositionMappingStrategy statusColumnMapping() {
        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
        strategy.setType(Status.class);
        String[] columns = new String[]{"created_at", "it", "text", "displayTextRangeStart", "displayTextRangeEnd", "source", "truncated", "inReplyToStatus", "inReplyToUserId", "inReplyToScreenName", "geoLocation", "place", "favorited", "retweeted", "favoriteCount", "user", "retweet", "retweetedStatus", "contributors", "retweetCount", "retweetedByMe", "currentUserRetweetId", "possiblySensitive", "lang", "scopes", "withheldInCountries", "quotedStatusId", "quotedStatus", "quotedStatusPermalink"};
        strategy.setColumnMapping(columns);
        return strategy;
    }

    @SuppressWarnings("unchecked")
    public void queryHandle(String handle) throws TwitterException, IOException {
        statuses.clear();
        fetchTweets();
        int counter = statuses.size();
        while (counter-- > 0) {
            System.out.println("Tweet #" + counter + ": " + statuses.get(counter).getText());
        }
    }

    public void fetchTweets() throws TwitterException, IOException {
        Paging page = new Paging(1, 10);
        int p = 1;
        Query query = new Query("doge OR dogecoin");
        query.setResultType(Query.ResultType.popular);
        query.setCount(10);
        query.setLang("en");
        QueryResult result;
        result = twitter.search(query);
        List<Status> tweets = result.getTweets();
        for (Status tweet : tweets) {
            System.out.println("@" + tweet.getUser().getScreenName());
            System.out.println(tweet.getText());
            System.out.println("___");
            System.out.println(tweet.getCreatedAt() + " | Likes: " + tweet.getFavoriteCount() + " | Retweets: " + tweet.getRetweetCount());
            System.out.println("_______________");
        }
        Writer writer = new FileWriter("src/main/resources/com/millerk97/csvtest.csv");
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).withMappingStrategy(statusColumnMapping()).build();
        try {
            beanToCsv.write(tweets);
        } catch (CsvDataTypeMismatchException e) {
            e.printStackTrace();
        } catch (CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
        }
        writer.close();
    }


}

