package com.millerk97.ais.cryptocompare;

import com.millerk97.ais.cryptocompare.domain.ohlc.APIResult;

public interface CryptocompareApiClient {

    APIResult getDailyOHLC(String exchange, String base, String target, Integer limit, Integer before);

    APIResult getHourlyOHLC(String exchange, String base, String target, Integer limit, Integer before);
}
