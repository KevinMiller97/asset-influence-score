package com.millerk97.ais.cryptocompare.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.coingecko.domain.Shared.Ticker;
import com.millerk97.ais.cryptocompare.CryptocompareApiClient;
import com.millerk97.ais.cryptocompare.domain.ohlc.APIResult;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLC;
import com.millerk97.ais.cryptocompare.domain.ohlc.OHLCList;

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
    private static final String OHLC_DAILY_TEMPLATE = "%s_%s_%s_daily.json";
    private static final String OHLC_HOURLY_TEMPLATE = "%s_%s_%s_hourly.json";
    private static final Integer DAILY_LIMIT = 1100;
    private static final Integer HOURLY_LIMIT = 2000;
    private static final Integer LENGTH_OF_DAY = 86400;

    private static final CryptocompareApiClient api = new CryptocompareApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void fetchOHLC(String cryptocurrency, String ticker, Integer start, Integer end, boolean forceReload) {
        Ticker mostRelevantTradingPair = com.millerk97.ais.coingecko.impl.DataFetcher.getMostRelevantTradingPair(ticker);
        fetchDailyOHLC(cryptocurrency, mostRelevantTradingPair, end, forceReload);
        fetchHourlyOHLC(cryptocurrency, mostRelevantTradingPair, end, start, forceReload);
    }

    private static void fetchDailyOHLC(String cryptocurrency, Ticker tradingPair, Integer before, boolean forceReload) {
        String fileNameDaily = getFileNameDaily(cryptocurrency, tradingPair);
        File dir = new File(PREFIX + String.format(DAILY_DIR, cryptocurrency));
        if (forceReload && dir != null) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }

        if (new File(fileNameDaily).exists() && !forceReload) {
            System.out.printf("Daily OHLC for %s already stored locally, not fetching from API%n", cryptocurrency);
            return;
        }

        try {
            // makes sure the directory exists, doesn't do anything if it already does
            dir.mkdirs();
            FileWriter fWriter = new FileWriter(fileNameDaily);
            System.out.printf("Fetching Daily OHLC data from cryptocompare API | Exchange: %s, Base: %s, Target: %s, Before Timestamp %s%n", tradingPair.getMarket().getName(), tradingPair.getBase(), tradingPair.getTarget(), before);
            APIResult result = api.getDailyOHLC(tradingPair.getMarket().getName(), tradingPair.getBase(), tradingPair.getTarget(), DAILY_LIMIT, before);
            OHLCList ohlcList = new OHLCList();
            ohlcList.setOhlcData(result.getData().getData());
            fWriter.write(mapper.writeValueAsString(ohlcList));
            fWriter.flush();
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fetchHourlyOHLC(String cryptocurrency, Ticker tradingPair, Integer before, Integer until, boolean forceReload) {
        String fileNameHourly = getFileNameHourly(cryptocurrency, tradingPair);

        File dir = new File(PREFIX + String.format(HOURLY_DIR, cryptocurrency));
        if (forceReload && dir != null) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }

        // since this function only fetches data to local storage we return
        if (new File(fileNameHourly).exists() && !forceReload) {
            System.out.printf("Hourly OHLC for %s already stored locally, not fetching from API%n", cryptocurrency);
            return;
        }

        // fetch hourly OHLC data for the respective cryptocurrency
        try {
            APIResult result;
            long currentLowerLimit = before;
            List<OHLC> hourlies = new ArrayList<>();
            do {
                System.out.printf("Fetching Hourly OHLC data from cryptocompare API | Exchange: %s, Base: %s, Target: %s, Before Timestamp %s%n", tradingPair.getMarket().getName(), tradingPair.getBase(), tradingPair.getTarget(), currentLowerLimit);
                result = api.getHourlyOHLC(tradingPair.getMarket().getName(), tradingPair.getBase(), tradingPair.getTarget(), HOURLY_LIMIT, (int) currentLowerLimit);
                hourlies.addAll(Arrays.stream(result.getData().getData()).toList());
                currentLowerLimit = result.getData().getTimeFrom();

            } while (currentLowerLimit > until);

            OHLCList ohlcList = new OHLCList();
            ohlcList.setOhlcData(hourlies.toArray(new OHLC[0]));

            // makes sure the directory exists, doesn't do anything if it already does
            dir.mkdirs();
            FileWriter fWriter = new FileWriter(fileNameHourly);
            fWriter.write(mapper.writeValueAsString(ohlcList));
            fWriter.flush();
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileNameDaily(String cryptocurrency, Ticker mrtp) {
        return PREFIX + String.format(DAILY_DIR, cryptocurrency) + String.format(OHLC_DAILY_TEMPLATE, mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget());
    }

    private static String getFileNameHourly(String cryptocurrency, Ticker mrtp) {
        return PREFIX + String.format(HOURLY_DIR, cryptocurrency) + String.format(OHLC_HOURLY_TEMPLATE, mrtp.getMarket().getName(), mrtp.getBase(), mrtp.getTarget());
    }

    public static List<OHLC> getHourlyOHLCForDay(String cryptocurrency, String ticker, OHLC day) {
        return getHourlyOHLCForTimeframe(cryptocurrency, ticker, day.getTime(), day.getTime() + LENGTH_OF_DAY);
    }

    public static List<OHLC> getHourlyOHLCForTimeframe(String cryptocurrency, String ticker, long start, long end) {
        Ticker mrtp = com.millerk97.ais.coingecko.impl.DataFetcher.getMostRelevantTradingPair(ticker);
        try {
            return filterTimeframe(Arrays.stream(mapper.readValue(Files.readString(Path.of(getFileNameHourly(cryptocurrency, mrtp))), OHLCList.class).getOhlcData()).toList(), start, end);
        } catch (IOException e) {
            System.err.printf("Could not fetch hourly OHLC for Timeframe %s - %s", start, end);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<OHLC> getDailyOHLCForTimeframe(String cryptocurrency, String ticker, long start, long end) {
        Ticker mrtp = com.millerk97.ais.coingecko.impl.DataFetcher.getMostRelevantTradingPair(ticker);
        try {
            return filterTimeframe(Arrays.stream(mapper.readValue(Files.readString(Path.of(getFileNameDaily(cryptocurrency, mrtp))), OHLCList.class).getOhlcData()).toList(), start, end);
        } catch (IOException e) {
            System.err.printf("Could not fetch hourly OHLC for Timeframe %s - %s", start, end);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<OHLC> filterTimeframe(List<OHLC> result, long start, long end) {
        return result.stream().filter(data -> (data.getTime() >= start && data.getTime() <= end)).collect(Collectors.toList());
    }

}
