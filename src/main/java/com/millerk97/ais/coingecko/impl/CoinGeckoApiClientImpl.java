package com.millerk97.ais.coingecko.impl;

import com.millerk97.ais.coingecko.CoinGeckoApi;
import com.millerk97.ais.coingecko.CoinGeckoApiClient;
import com.millerk97.ais.coingecko.CoinGeckoApiService;
import com.millerk97.ais.coingecko.coins.CoinFullData;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangeById;
import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesList;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesTickersById;
import com.millerk97.ais.coingecko.domain.MarketChart.MarketChart;
import com.millerk97.ais.coingecko.domain.Ping;
import com.millerk97.ais.coingecko.global.Global;

import java.util.List;
import java.util.Map;

public class CoinGeckoApiClientImpl implements CoinGeckoApiClient {
    static final Long DEFAULT_CONNECTION_TIMEOUT = 10L;
    static final Long DEFAULT_READ_TIMEOUT = 10L;
    static final Long DEFAULT_WRITE_TIMEOUT = 10L;

    private final CoinGeckoApiService coinGeckoApiService;
    private final CoinGeckoApi coinGeckoApi;

    public CoinGeckoApiClientImpl() {
        this(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT);
    }

    public CoinGeckoApiClientImpl(Long connectionTimeoutSeconds, Long readTimeoutSeconds, Long writeTimeoutSeconds) {
        this.coinGeckoApi = new CoinGeckoApi();
        this.coinGeckoApiService = coinGeckoApi.createService(
                CoinGeckoApiService.class,
                connectionTimeoutSeconds,
                readTimeoutSeconds,
                writeTimeoutSeconds
        );
    }

    @Override
    public Ping ping() {
        return coinGeckoApi.executeSync(coinGeckoApiService.ping());
    }

    @Override
    public Map<String, Map<String, Double>> getPrice(String ids, String vsCurrencies, boolean includeMarketCap, boolean include24hrVol, boolean include24hrChange, boolean includeLastUpdatedAt) {
        return coinGeckoApi.executeSync(coinGeckoApiService.getPrice(ids, vsCurrencies, includeMarketCap, include24hrVol, include24hrChange, includeLastUpdatedAt));
    }

    @Override
    public Global getGlobal() {
        return coinGeckoApi.executeSync(coinGeckoApiService.getGlobal());
    }

    @Override
    public List<Exchanges> getExchanges() {
        return getExchanges(100, 0);
    }

    @Override
    public List<Exchanges> getExchanges(int perPage, int page) {
        return coinGeckoApi.executeSync(coinGeckoApiService.getExchanges(perPage, page));
    }

    @Override
    public List<ExchangesList> getExchangesList() {
        return coinGeckoApi.executeSync(coinGeckoApiService.getExchangesList());
    }

    @Override
    public ExchangeById getExchangesById(String id) {
        return coinGeckoApi.executeSync(coinGeckoApiService.getExchangesById(id));
    }

    @Override
    public ExchangesTickersById getExchangesTickersById(String id) {
        return getExchangesTickersById(id, null, null, null);
    }

    @Override
    public ExchangesTickersById getExchangesTickersById(String id, String coinIds, Integer page, String order) {
        return coinGeckoApi.executeSync(coinGeckoApiService.getExchangesTickersById(id, coinIds, page, order));
    }

    @Override
    public List<List<String>> getExchangesVolumeChart(String id, Integer days) {
        return coinGeckoApi.executeSync(coinGeckoApiService.getExchangesVolumeChart(id, days));
    }

    @Override
    public ExchangesTickersById getTickers(String id, int page) {
        return coinGeckoApi.executeSync((coinGeckoApiService.getTickers(id, page)));
    }

    @Override
    public CoinFullData getCoinById(String id) {
        return coinGeckoApi.executeSync(coinGeckoApiService.getCoinById(id));
    }

    @Override
    public MarketChart getMarketChart(String id, String targetCurrency, String from, String to) {
        return coinGeckoApi.executeSync(coinGeckoApiService.getMarketChart(id, targetCurrency, from, to));
    }

    @Override
    public void shutdown() {
        coinGeckoApi.shutdown();
    }
}
