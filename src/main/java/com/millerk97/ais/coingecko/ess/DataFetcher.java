package com.millerk97.ais.coingecko.ess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.coingecko.CoinGeckoApiClient;
import com.millerk97.ais.coingecko.coins.CoinFullData;
import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesList;
import com.millerk97.ais.coingecko.impl.CoinGeckoApiClientImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DataFetcher {

    private static final String PREFIX = "src/main/resources/com/millerk97/data/";
    private static final String COIN_FULLDATA = "%s_fulldata.json";
    private static final String EXCHANGES_LIST = "exchanges_list.json";
    private static final String EXCHANGES = "exchanges.json";

    private static final CoinGeckoApiClient api = new CoinGeckoApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Optional<CoinFullData> fullDataOptional = Optional.empty();

    public static Set<Exchanges> getSupportedExchanges(String cryptocurrency) {
        fetchCoinFullData(cryptocurrency);
        return fetchExchanges().stream().filter(exchange -> getSupportedExchangeIds().contains(exchange.getId())).collect(Collectors.toSet());
    }

    // TODO there is still a lot of code duplication, find a way to make local read/api call reusable

    private static void fetchCoinFullData(String cryptocurrency) {
        String fileName = PREFIX + String.format(COIN_FULLDATA, cryptocurrency);
        try {
            if (new File(fileName).exists()) {
                System.out.println("Reading Full Data for " + cryptocurrency + " from local storage");
                fullDataOptional = Optional.of(mapper.readValue(Files.readString(Path.of(fileName)), CoinFullData.class));
            } else {
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println("Full Data for " + cryptocurrency + " not stored locally, fetching from API");
                fullDataOptional = Optional.of(api.getCoinById(cryptocurrency));
                fWriter.write(mapper.writeValueAsString(fullDataOptional.get()));
                fWriter.flush();
                fWriter.close();
                System.out.println("Created Full Data local store for " + cryptocurrency);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<ExchangesList> fetchExchangesList() {
        String fileName = PREFIX + EXCHANGES_LIST;
        try {
            if (new File(fileName).exists()) {
                return Arrays.asList(mapper.readValue(Files.readString(Path.of(fileName)), ExchangesList[].class));
            } else {
                List<ExchangesList> exchangesList;
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println("Fetching ExchangesList from API");
                exchangesList = api.getExchangesList();
                fWriter.write(mapper.writeValueAsString(exchangesList));
                fWriter.flush();
                fWriter.close();
                System.out.println("Created Full Data local store ExchangesList");
                return exchangesList;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<Exchanges> fetchExchanges() {
        String fileName = PREFIX + EXCHANGES;
        try {
            if (new File(fileName).exists()) {
                return Arrays.asList(mapper.readValue(Files.readString(Path.of(fileName)), Exchanges[].class));
            } else {
                List<Exchanges> exchanges;
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println("Fetching Exchanges from API");
                exchanges = api.getExchanges();
                fWriter.write(mapper.writeValueAsString(exchanges));
                fWriter.flush();
                fWriter.close();
                System.out.println("Created Full Data local store Exchanges");
                return exchanges;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static Set<String> getSupportedExchangesAsString() {
        return fullDataOptional.isPresent() ? fullDataOptional.get().getTickers().stream().map(t -> t.getMarket().getName()).collect(Collectors.toSet()) : new HashSet<>();
    }

    private static List<String> getSupportedExchangeIds() {
        return fetchExchangesList().stream().filter(exchange -> getSupportedExchangesAsString().contains(exchange.getName())).map(exchange -> exchange.getId()).collect(Collectors.toList());
    }
}
