package com.millerk97.ais.coingecko.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millerk97.ais.coingecko.CoinGeckoApiClient;
import com.millerk97.ais.coingecko.coins.CoinFullData;
import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesList;
import com.millerk97.ais.coingecko.domain.MarketChart.MarketChart;
import com.millerk97.ais.coingecko.domain.Shared.Ticker;
import com.millerk97.ais.coingecko.exception.CoinGeckoApiException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DataFetcher {

    private static final String PREFIX = "src/main/resources/com/millerk97/data/";
    private static final String COIN_FULLDATA = "%s_fulldata.json";
    private static final String EXCHANGES_LIST = "exchanges_list.json";
    private static final String EXCHANGES = "exchanges.json";
    private static final String COIN_MCAP = "%s_mcap.json";
    private static final String GLOBAL_MCAP = "global_mcap.csv";
    private static final Long DURATION_DAY = 86400L;

    private static final CoinGeckoApiClient api = new CoinGeckoApiClientImpl();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Optional<CoinFullData> fullDataOptional = Optional.empty();

    public static Set<Exchanges> getSupportedExchanges(String cryptocurrency, boolean reload) {
        fetchCoinFullData(cryptocurrency.toLowerCase(), reload);
        return fetchExchanges().stream().filter(exchange -> getSupportedExchangeIds().contains(exchange.getId())).collect(Collectors.toSet());
    }

    /* currently only take binance, as I deem it most trustworthy */
    public static Ticker getMostRelevantTradingPair(String ticker) {
        fetchCoinFullData(ticker, false);
        return fullDataOptional.get().getTickers().stream().filter(t -> t.getBase().equals(ticker)).filter(t -> t.getTarget().equals("USDT")).sorted(Comparator.reverseOrder()).filter(t -> t.getMarket().getName().equals("Binance")).toList().get(0);
    }

    public static MarketChart getMarketChart(String cryptocurrency) {
        String fileName = PREFIX + String.format(COIN_MCAP, cryptocurrency.toLowerCase());
        try {
            if (new File(fileName).exists()) {
                return mapper.readValue(Files.readString(Path.of(fileName)), MarketChart.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MarketChart();
    }

    public static MarketChart fetchMarketCap(String cryptocurrency, Long from, Long to, boolean reload) {
        String fileName = PREFIX + String.format(COIN_MCAP, cryptocurrency.toLowerCase());
        MarketChart marketChart;
        try {
            if (new File(fileName).exists() && !Files.readString(Path.of(fileName)).isBlank() && !reload) {
                System.out.println("Fetching market cap data for " + cryptocurrency + " from local storage");
                marketChart = mapper.readValue(Files.readString(Path.of(fileName)), MarketChart.class);
            } else {
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println("Market cap for " + cryptocurrency + " not stored locally, fetching from API");
                marketChart = api.getMarketChart(cryptocurrency.toLowerCase(), "usd", String.valueOf(from), String.valueOf(to));
                fWriter.write(mapper.writeValueAsString(marketChart));
                fWriter.flush();
                fWriter.close();
                System.out.println("Created market cap data for " + cryptocurrency);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new MarketChart();
        }
        return marketChart;
    }

    public static double getMarketCap(MarketChart marketChart, Long timestamp) {
        for (String[] entry : marketChart.getMcaps()) {
            Long ts = Long.parseLong(entry[0]) / 1000;
            double value = Double.parseDouble(entry[1]);
            if (timestamp >= ts && timestamp <= ts + DURATION_DAY) {
                return value;
            }
        }
        System.err.println("could not find market cap for timestamp " + timestamp);
        return 0d;
    }

    public static List<List<String>> fetchGlobalMarketCap(Long from, Long to) {
        final SimpleDateFormat timestampCreator = new SimpleDateFormat("yyyy-MM-dd");
        List<List<String>> entries = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(PREFIX + GLOBAL_MCAP))) {
            while (scanner.hasNextLine()) {
                List<String> nextLine = getRecordFromLine(scanner.nextLine());
                Long timestamp = timestampCreator.parse(nextLine.get(0)/*Date*/).getTime() / 1000;
                if (from <= timestamp && to >= timestamp)
                    entries.add(getRecordFromLine(scanner.nextLine()));
            }
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static double getGlobalMarketCap(List<List<String>> entries, Long timestamp) {
        final SimpleDateFormat timestampCreator = new SimpleDateFormat("yyyy-MM-dd");
        try {
            for (List<String> line : entries) {
                Long ts = timestampCreator.parse(line.get(0)/*Date*/).getTime() / 1000;
                // timestamp gets smaller and smaller, so as soon as it is there we found the right line
                if (timestamp <= ts && timestamp >= ts - DURATION_DAY) {
                    return Double.parseDouble(line.get(1) /*Market Cap*/) / Double.parseDouble(line.get(2)/*Mcap Dominance*/) * 100;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 1000000000000d; // 1 Trillion, rough average
    }

    private static List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }


    private static void fetchCoinFullData(String cryptocurrency, boolean forceReload) {
        String fileName = PREFIX + String.format(COIN_FULLDATA, cryptocurrency);
        try {
            if (new File(fileName).exists() && !forceReload) {
                fullDataOptional = Optional.of(mapper.readValue(Files.readString(Path.of(fileName)), CoinFullData.class));
            } else {
                FileWriter fWriter = new FileWriter(fileName);
                System.out.println("Full Data for " + cryptocurrency + " not stored locally, fetching from API");
                try {
                    fullDataOptional = Optional.of(api.getCoinById(cryptocurrency));
                    // add additional tickers beyond 100 to ensure good coverage
                    fullDataOptional.get().getTickers().addAll(api.getTickers(cryptocurrency, 2).getTickers());
                    fullDataOptional.get().getTickers().addAll(api.getTickers(cryptocurrency, 3).getTickers());
                } catch (CoinGeckoApiException e) {
                    // ignore, no more pages
                }
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
        return fullDataOptional.map(coinFullData -> coinFullData.getTickers().stream().map(t -> t.getMarket().getName()).collect(Collectors.toSet())).orElseGet(HashSet::new);
    }

    private static List<String> getSupportedExchangeIds() {
        return fetchExchangesList().stream().filter(exchange -> getSupportedExchangesAsString().contains(exchange.getName())).map(ExchangesList::getId).collect(Collectors.toList());
    }
}
