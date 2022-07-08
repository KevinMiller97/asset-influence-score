package com.millerk97.ais.cryptowatch;

import com.millerk97.ais.cryptowatch.domain.Exchange.ExchangeList;
import com.millerk97.ais.cryptowatch.domain.market.MarketList;
import com.millerk97.ais.cryptowatch.domain.ohlc.OHLCResult;

public interface CryptowatchApiClient {

    ExchangeList getExchanges();

    MarketList getMarkets();

    OHLCResult getDailyOHLC(String exchange, String pair, Integer before, Integer after);

    OHLCResult getHourlyOHLC(String exchange, String pair, Integer before, Integer after);
}
