package com.millerk97.ais.cryptocompare.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.coingecko.domain.Shared.Ticker;
import com.millerk97.ais.cryptocompare.CryptocompareApiClient;
import com.millerk97.ais.cryptocompare.domain.ohlc.APIResult;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DataFetcher {

    private static final String PREFIX = "src/main/resources/com/millerk97/ohlc";
    private static final String DAILY_DIR = "/%s_daily/";
    private static final String HOURLY_DIR = "/%s_hourly/";
    private static final String OHLC_DAILY_TEMPLATE = "%s_%s_%s_%d_daily.json";
    private static final String OHLC_HOURLY_TEMPLATE = "%s_%d_%d_hourly.json";
    private static final Integer DAILY_LIMIT = 1100;
    private static final Integer HOURLY_LIMIT = 2000;
    private static final Integer LENGTH_OF_DAY = 86400;
    private static final Integer LENGTH_OF_HOUR = 3600;

    private static final CryptocompareApiClient api = new CryptocompareApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<OHLC> fetchDailyOHLC(String cryptocurrency, String ticker, Integer before) {
        Ticker mrtp = com.millerk97.ais.coingecko.impl.DataFetcher.getMostRelevantTradingPair(ticker);
        String fileName = PREFIX + String.format(DAILY_DIR, cryptocurrency) + String.format(OHLC_DAILY_TEMPLATE, mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget(), before);

        try {
            if (new File(fileName).exists()) {
                APIResult result = mapper.readValue(Files.readString(Path.of(fileName)), APIResult.class);
                // also fetch hourly data
                fetchHourlyOHLC(cryptocurrency, ticker, before, (int) result.getData().getTimeFrom());
                return getDailyOHLCListFromResult(result);
            } else {
                // makes sure the directory exists, doesn't do anything if it already does
                new File(PREFIX + String.format(DAILY_DIR, cryptocurrency)).mkdirs();

                FileWriter fWriter = new FileWriter(fileName);
                System.out.println(String.format("Fetching Daily OHLC data from cryptocompare API | Exchange: %s, Base: %s, Target: %s, Before Timestamp %s", mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget(), before));
                APIResult result = api.getDailyOHLC(mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget(), DAILY_LIMIT, before);
                fWriter.write(mapper.writeValueAsString(result));
                fWriter.flush();
                fWriter.close();
                // also fetch hourly data
                fetchHourlyOHLC(cryptocurrency, ticker, before, (int) result.getData().getTimeFrom());
                return getDailyOHLCListFromResult(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static void fetchHourlyOHLC(String cryptocurrency, String ticker, Integer before, Integer until) {
        Ticker mrtp = com.millerk97.ais.coingecko.impl.DataFetcher.getMostRelevantTradingPair(ticker);

        // check if a file exists, if it does, subsequent ones will as well
        if (new File(PREFIX + String.format(HOURLY_DIR, cryptocurrency) + String.format(OHLC_HOURLY_TEMPLATE, cryptocurrency, before - HOURLY_LIMIT * LENGTH_OF_HOUR, before)).exists()) {
            return;
        }

        // fetch hourly OHLC data for the respective cryptocurrency
        try {
            APIResult result;
            long currentLowerLimit = before;
            do {
                System.out.println(String.format("Fetching Hourly OHLC data from cryptocompare API | Exchange: %s, Base: %s, Target: %s, Before Timestamp %s", mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget(), currentLowerLimit));
                result = api.getHourlyOHLC(mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget(), HOURLY_LIMIT, (int) currentLowerLimit);
                String fileName = PREFIX + String.format(HOURLY_DIR, cryptocurrency) + String.format(OHLC_HOURLY_TEMPLATE, cryptocurrency, result.getData().getTimeFrom(), result.getData().getTimeTo());
                currentLowerLimit = result.getData().getTimeFrom();


                // makes sure the directory exists, doesn't do anything if it already does
                new File(PREFIX + String.format(HOURLY_DIR, cryptocurrency)).mkdirs();
                FileWriter fWriter = new FileWriter(fileName);
                fWriter.write(mapper.writeValueAsString(result));
                fWriter.flush();
                fWriter.close();
            } while (currentLowerLimit > until);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<OHLC> getHourlyOHLCsForDay(String cryptocurrency, OHLC day) {
        List<OHLC> hourly = new ArrayList<>();

        long start = day.getTime();
        long end = day.getTime() + LENGTH_OF_DAY;
        // find corresponding hourly file with timeframe;
        for (File hourlyJson : new File(PREFIX + String.format(HOURLY_DIR, cryptocurrency)).listFiles()) {
            // [0] .. cryptocurrency; [1] .. {from}; [2] .. {to}; [3] .. ".json"
            String[] timestamps = hourlyJson.getName().split("[_.]");
            if (start >= Long.parseLong(timestamps[1]) && end <= Long.parseLong(timestamps[2])) {
                // correct file
                try {
                    APIResult result = mapper.readValue(Files.readString(Path.of(PREFIX + String.format(HOURLY_DIR, cryptocurrency) + hourlyJson.getName())), APIResult.class);
                    hourly.addAll(filterTimeframe(result, start, end));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hourly;
    }

    private static List<OHLC> getDailyOHLCListFromResult(APIResult result) {
        return Arrays.asList(result.getData().getData());
    }

    private static List<OHLC> filterTimeframe(APIResult result, long start, long end) {
        return Arrays.stream(result.getData().getData()).filter(data -> (data.getTime() >= start && data.getTime() <= end)).collect(Collectors.toList());
    }

}
