package com.millerk97.ais.coingecko.impl;

import com.millerk97.ais.coingecko.CoinGeckoApi;
import com.millerk97.ais.coingecko.CoinGeckoApiClient;
import com.millerk97.ais.coingecko.CoinGeckoApiService;
import com.millerk97.ais.coingecko.coins.CoinFullData;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangeById;
import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesList;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesTickersById;
import com.millerk97.ais.coingecko.domain.Ping;

import java.util.List;

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
    public void shutdown() {
        coinGeckoApi.shutdown();
    }
}
