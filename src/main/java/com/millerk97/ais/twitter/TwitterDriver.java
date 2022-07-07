package com.millerk97.ais.twitter;
/**
 * @author Ria Galanos
 **/

import com.millerk97.ais.coingecko.CoinGeckoApiClient;
import com.millerk97.ais.coingecko.impl.CoinGeckoApiClientImpl;
import com.millerk97.ais.cryptowatch.CryptowatchApiClient;
import com.millerk97.ais.cryptowatch.impl.CryptowatchApiClientImpl;
import twitter4j.TwitterException;

import java.io.IOException;
import java.io.PrintStream;

public class TwitterDriver {
    private static PrintStream consolePrint;

    public static void main(String[] args) throws TwitterException, IOException {
        // set up classpath and properties file
        Twitterer bigBird = new Twitterer(consolePrint);

        CoinGeckoApiClient client = new CoinGeckoApiClientImpl();

        CryptowatchApiClient cwatch = new CryptowatchApiClientImpl();
        // System.out.println(cwatch.getExchanges());
        // System.out.println(cwatch.getMarkets());


        client.shutdown();
        int threshold = 20; // might be changed
        double btcess = 0;

        for (double i = threshold; i > 0; i--) {
            btcess += threshold - i;
        }
        double btcmcr = 580.23d / 1366.7d;
        System.out.println(btcess / btcmcr);


        double ethess = 0;

        for (double i = threshold; i > 0; i--) {
            ethess += threshold - i;
        }
        double ethmcr = 249.5d / 1366.7d;
        System.out.println((ethess / ethmcr) / 0.9d);
        // System.out.println(ethmcr);

        double adaess = 0;

        for (double i = threshold; i > 0; i--) {
            adaess += threshold - i;
        }
        double adamcr = 249.5d / 1366.7d;
        // System.out.println((adaess / adamcr) / 0.9d);


        /*
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter a Twitter handle, do not include the '@' symbol (or 'done' to quit.)");
        String twitter_handle = scan.next();
        while (!"done".equals(twitter_handle)) {
            bigBird.queryHandle(twitter_handle);
            System.out.print("Please enter a Twitter handle, do not include the '@' symbol (or 'done' to quit.)");
            twitter_handle = scan.next();

        }
        */
    }

}