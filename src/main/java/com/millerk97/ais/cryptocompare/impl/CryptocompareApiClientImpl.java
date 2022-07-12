package com.millerk97.ais.cryptocompare.impl;

import com.millerk97.ais.cryptocompare.CryptocompareApi;
import com.millerk97.ais.cryptocompare.CryptocompareApiClient;
import com.millerk97.ais.cryptocompare.CryptocompareApiService;
import com.millerk97.ais.cryptocompare.domain.ohlc.APIResult;

public class CryptocompareApiClientImpl implements CryptocompareApiClient {
    static final Long DEFAULT_CONNECTION_TIMEOUT = 10L;
    static final Long DEFAULT_READ_TIMEOUT = 10L;
    static final Long DEFAULT_WRITE_TIMEOUT = 10L;

    private final CryptocompareApiService cryptowatchApiService;
    private final CryptocompareApi cryptowatchApi;

    public CryptocompareApiClientImpl() {
        this(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT);
    }

    public CryptocompareApiClientImpl(Long connectionTimeoutSeconds, Long readTimeoutSeconds, Long writeTimeoutSeconds) {
        this.cryptowatchApi = new CryptocompareApi();
        this.cryptowatchApiService = cryptowatchApi.createService(
                CryptocompareApiService.class,
                connectionTimeoutSeconds,
                readTimeoutSeconds,
                writeTimeoutSeconds
        );
    }

    @Override
    public APIResult getDailyOHLC(String exchange, String base, String target, Integer limit, Integer before) {
        return cryptowatchApi.executeSync(cryptowatchApiService.getDailyOHLC(exchange, base, target, limit, before));
    }

    @Override
    public APIResult getHourlyOHLC(String exchange, String base, String target, Integer limit, Integer before) {
        return cryptowatchApi.executeSync(cryptowatchApiService.getHourlyOHLC(exchange, base, target, limit, before));
    }
}
