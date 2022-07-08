package com.millerk97.ais.cryptowatch.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.coingecko.coins.CoinFullData;
import com.millerk97.ais.coingecko.domain.Shared.Ticker;
import com.millerk97.ais.cryptowatch.CryptowatchApiClient;
import com.millerk97.ais.cryptowatch.domain.ohlc.OHLC;
import com.millerk97.ais.cryptowatch.domain.ohlc.OHLCResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DataFetcher {

    private static final String PREFIX = "src/main/resources/com/millerk97/ohlc/";
    private static final String COIN_FULLDATA = "%s_fulldata.json";
    private static final String COIN_MCAP = "%s_mcap.json";
    private static final String OHLC_TEMPLATE = "%s_%s_%d_%d.json";
    private static final String EXCHANGES = "exchanges.json";
    private static final String GLOBAL = "global.json";

    private static final CryptowatchApiClient api = new CryptowatchApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Optional<CoinFullData> fullDataOptional = Optional.empty();

    public static List<OHLC> getDailyOHLC(String cryptocurrency, Integer before, Integer after) {
        Ticker mostRelevantTradingPair = com.millerk97.ais.coingecko.impl.DataFetcher.getMostRelevantTradingPair(cryptocurrency);
        String fileName = PREFIX + String.format(OHLC_TEMPLATE, cryptocurrency, mostRelevantTradingPair.getMarket().getName(), before, after);

        try {
            if (new File(fileName).exists()) {
                return getDailyOHLCListFromResult(mapper.readValue(Files.readString(Path.of(fileName)), OHLCResult.class));
            } else {
                FileWriter fWriter = new FileWriter(fileName);
                String pair = (mostRelevantTradingPair.getBase() + mostRelevantTradingPair.getTarget()).toLowerCase(Locale.ROOT);
                System.out.println(String.format("Fetching OHLC data from %s until %s for %s for Exchange: %s from API", after, before, pair, mostRelevantTradingPair.getMarket().getName()));
                OHLCResult result = api.getDailyOHLC(mostRelevantTradingPair.getMarket().getName().toLowerCase(Locale.ROOT), pair, before, after);
                fWriter.write(mapper.writeValueAsString(result));
                fWriter.flush();
                fWriter.close();
                System.out.println("Created local store for OHLC Candles for " + cryptocurrency);
                return getDailyOHLCListFromResult(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private static List<OHLC> getDailyOHLCListFromResult(OHLCResult result) {
        return Arrays.asList(result.getResult().get("86400")).stream().map(ohlc -> OHLC.of(ohlc)).collect(Collectors.toList());
    }
}
