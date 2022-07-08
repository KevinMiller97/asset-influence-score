package com.millerk97.ais.cryptowatch.impl;

import com.millerk97.ais.cryptowatch.CryptowatchApi;
import com.millerk97.ais.cryptowatch.CryptowatchApiClient;
import com.millerk97.ais.cryptowatch.CryptowatchApiService;
import com.millerk97.ais.cryptowatch.domain.Exchange.ExchangeList;
import com.millerk97.ais.cryptowatch.domain.market.MarketList;
import com.millerk97.ais.cryptowatch.domain.ohlc.OHLCResult;

public class CryptowatchApiClientImpl implements CryptowatchApiClient {
    static final Long DEFAULT_CONNECTION_TIMEOUT = 10L;
    static final Long DEFAULT_READ_TIMEOUT = 10L;
    static final Long DEFAULT_WRITE_TIMEOUT = 10L;

    private final CryptowatchApiService cryptowatchApiService;
    private final CryptowatchApi cryptowatchApi;

    public CryptowatchApiClientImpl() {
        this(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT);
    }

    public CryptowatchApiClientImpl(Long connectionTimeoutSeconds, Long readTimeoutSeconds, Long writeTimeoutSeconds) {
        this.cryptowatchApi = new CryptowatchApi();
        this.cryptowatchApiService = cryptowatchApi.createService(
                CryptowatchApiService.class,
                connectionTimeoutSeconds,
                readTimeoutSeconds,
                writeTimeoutSeconds
        );
    }

    @Override
    public ExchangeList getExchanges() {
        return cryptowatchApi.executeSync(cryptowatchApiService.getExchanges());
    }

    @Override
    public MarketList getMarkets() {
        return cryptowatchApi.executeSync(cryptowatchApiService.getMarkets());
    }

    @Override
    public OHLCResult getDailyOHLC(String exchange, String pair, Integer before, Integer after) {
        return cryptowatchApi.executeSync(cryptowatchApiService.getDailyOHLC(exchange, pair, before, after));
    }

    @Override
    public OHLCResult getHourlyOHLC(String exchange, String pair, Integer before, Integer after) {
        return cryptowatchApi.executeSync(cryptowatchApiService.getHourlyOHLC(exchange, pair, before, after));
    }
}
