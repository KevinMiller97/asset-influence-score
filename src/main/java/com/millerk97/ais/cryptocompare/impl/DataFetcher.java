package com.millerk97.ais.cryptocompare.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.coingecko.domain.Shared.Ticker;
import com.millerk97.ais.cryptocompare.CryptocompareApiClient;
import com.millerk97.ais.cryptocompare.domain.ohlc.APIResult;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import javafx.util.Pair;

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
    private static final String DAILY_DIR = "/dogecoin_daily/";
    private static final String HOURLY_DIR = "/dogecoin_hourly/";
    private static final String OHLC_DAILY_TEMPLATE = "%s_%s_%s_%d_daily.json";
    private static final Integer DAILY_LIMIT = 1100;
    private static final Integer HOURLY_LIMIT = 2000;
    private static final Integer LENGTH_OF_DAY = 86400;

    private static final CryptocompareApiClient api = new CryptocompareApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<OHLC> getDailyOHLC(String cryptocurrency, Integer before) {
        Ticker mrtp = com.millerk97.ais.coingecko.impl.DataFetcher.getMostRelevantTradingPair(cryptocurrency);
        String fileName = PREFIX + String.format(OHLC_DAILY_TEMPLATE, mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget(), before);

        try {
            if (new File(fileName).exists()) {
                return getDailyOHLCListFromResult(mapper.readValue(Files.readString(Path.of(fileName)), APIResult.class));
            } else {
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println(String.format("Fetching Daily OHLC data from cryptocompare API | Exchange: %s, Base: %s, Target: %s, Before Timestamp %s", mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget(), before));
                APIResult result = api.getDailyOHLC(mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget(), DAILY_LIMIT, before);
                fWriter.write(mapper.writeValueAsString(result));
                fWriter.flush();
                fWriter.close();
                return getDailyOHLCListFromResult(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<OHLC> getDailyOHLCListFromResult(APIResult result) {
        return Arrays.asList(result.getData().getData());
    }


    public static List<Pair<OHLC, List<OHLC>>> getHourlyOHLCForDays(List<OHLC> days) {
        List<Pair<OHLC, List<OHLC>>> hourlies = new ArrayList<>();
        for (OHLC day : days) {
            hourlies.add(new Pair(day, getHourlyOHLCsForDay(day)));
        }
        return hourlies;
    }

    public static List<OHLC> getHourlyOHLCsForDay(OHLC day) {
        List<OHLC> hourly = new ArrayList<>();

        long start = day.getTime();
        long end = day.getTime() + LENGTH_OF_DAY;
        // find corresponding hourly file with timeframe;
        for (File hourlyJson : new File(PREFIX + HOURLY_DIR).listFiles()) {
            // [0] .. "from"; [1] .. {from}; [2] .. "to"; [3] .. {to}; [4] .. ".json"
            String[] timestamps = hourlyJson.getName().split("[_.]");
            if (start > Long.parseLong(timestamps[1]) && end < Long.parseLong(timestamps[3])) {
                // correct file
                try {
                    APIResult result = mapper.readValue(Files.readString(Path.of(PREFIX + HOURLY_DIR + hourlyJson.getName())), APIResult.class);
                    hourly.addAll(filterTimeframe(result, start, end));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hourly;
    }

    private static List<OHLC> filterTimeframe(APIResult result, long start, long end) {
        return Arrays.stream(result.getData().getData()).filter(data -> (data.getTime() >= start && data.getTime() < end)).collect(Collectors.toList());
    }


}
