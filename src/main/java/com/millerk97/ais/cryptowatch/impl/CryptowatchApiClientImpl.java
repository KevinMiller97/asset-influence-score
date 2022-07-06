package com.millerk97.ais.cryptowatch.impl;

import com.millerk97.ais.cryptowatch.CryptowatchApi;
import com.millerk97.ais.cryptowatch.CryptowatchApiClient;
import com.millerk97.ais.cryptowatch.CryptowatchApiService;
import com.millerk97.ais.cryptowatch.domain.Exchange.ExchangeList;
import com.millerk97.ais.cryptowatch.domain.OHLC;
import com.millerk97.ais.cryptowatch.domain.market.MarketList;

import java.util.List;

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
    public List<OHLC> getOHLC(String exchange, String pair, Integer before, Integer after) {
        //double[] candlesticks = cryptowatchApiService.getOHLC()
        return null;
    }

    @Override
    public ExchangeList getExchanges() {
        return cryptowatchApi.executeSync(cryptowatchApiService.getExchanges());
    }

    @Override
    public MarketList getMarkets() {
        return cryptowatchApi.executeSync(cryptowatchApiService.getMarkets());
    }
}
