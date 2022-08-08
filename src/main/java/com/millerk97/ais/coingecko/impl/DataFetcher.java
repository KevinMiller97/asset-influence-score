package com.millerk97.ais.coingecko.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.coingecko.CoinGeckoApiClient;
import com.millerk97.ais.coingecko.coins.CoinFullData;
import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesList;
import com.millerk97.ais.coingecko.domain.Shared.Ticker;
import com.millerk97.ais.coingecko.global.Global;

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
    private static final String COIN_MCAP = "%s_mcap.json";
    private static final String EXCHANGES_LIST = "exchanges_list.json";
    private static final String EXCHANGES = "exchanges.json";
    private static final String GLOBAL = "global.json";

    private static final CoinGeckoApiClient api = new CoinGeckoApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Optional<CoinFullData> fullDataOptional = Optional.empty();

    public static Set<Exchanges> getSupportedExchanges(String cryptocurrency, boolean forceReload) {
        fetchCoinFullData(cryptocurrency, forceReload);
        return fetchExchanges().stream().filter(exchange -> getSupportedExchangeIds().contains(exchange.getId())).collect(Collectors.toSet());
    }

    // TODO there is still a lot of code duplication, find a way to make local read/api call reusable

    public static double getMarketCap(String cryptocurrency, boolean forceReload) {
        String fileName = PREFIX + String.format(COIN_MCAP, cryptocurrency);
        try {
            if (new File(fileName).exists() && !forceReload) {
                return Double.parseDouble(Files.readString(Path.of(fileName)));
            } else {
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println("Fetching Market cap for " + cryptocurrency + " from API");
                double mcap = api.getPrice(cryptocurrency, "usd", true, false, false, false).get(cryptocurrency).get("usd_market_cap");
                fWriter.write("" + mcap);
                fWriter.flush();
                fWriter.close();
                System.out.println("Created Market cap local store for " + cryptocurrency);
                return mcap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getGlobalMarketCap(boolean forceReload) {
        return fetchGlobalMarketCap(forceReload).getData().getTotalMarketCap().get("usd");
    }

    /* currently only take binance, as I deem it most trustworthy */
    public static Ticker getMostRelevantTradingPair(String ticker) {
        fetchCoinFullData(ticker, false);
        return fullDataOptional.get().getTickers().stream().filter(t -> t.getBase().equals(ticker)).filter(t -> t.getTarget().equals("USDT")).sorted(Comparator.reverseOrder()).filter(t -> t.getMarket().getName().equals("Binance")).collect(Collectors.toList()).get(0);
    }


    private static Global fetchGlobalMarketCap(boolean forceReload) {
        String fileName = PREFIX + GLOBAL;
        Global global;
        try {
            if (new File(fileName).exists() && !forceReload) {
                System.out.println("Fetching Global market cap from local storage");
                global = mapper.readValue(Files.readString(Path.of(fileName)), Global.class);
            } else {
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println("Global market cap not stored locally, fetching from API");
                global = api.getGlobal();
                fWriter.write(mapper.writeValueAsString(global));
                fWriter.flush();
                fWriter.close();
                System.out.println("Created Full Data local store for global market cap");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Global();
        }
        return global;
    }

    private static void fetchCoinFullData(String cryptocurrency, boolean forceReload) {
        String fileName = PREFIX + String.format(COIN_FULLDATA, cryptocurrency);
        try {
            if (new File(fileName).exists() && !forceReload) {
                System.out.println("Reading Full Data for " + cryptocurrency + " from local storage");
                fullDataOptional = Optional.of(mapper.readValue(Files.readString(Path.of(fileName)), CoinFullData.class));
            } else {
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println("Full Data for " + cryptocurrency + " not stored locally, fetching from API");
                fullDataOptional = Optional.of(api.getCoinById(cryptocurrency));
                // add additional tickers beyond 100 to ensure good coverage
                fullDataOptional.get().getTickers().addAll(api.getTickers(cryptocurrency, 2).getTickers());
                fullDataOptional.get().getTickers().addAll(api.getTickers(cryptocurrency, 3).getTickers());
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
