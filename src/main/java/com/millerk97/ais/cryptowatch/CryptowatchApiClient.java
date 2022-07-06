package com.millerk97.ais.cryptowatch;

import com.millerk97.ais.cryptowatch.domain.Exchange.ExchangeList;
import com.millerk97.ais.cryptowatch.domain.OHLC;
import com.millerk97.ais.cryptowatch.domain.market.MarketList;

import java.util.List;

public interface CryptowatchApiClient {

    List<OHLC> getOHLC(String exchange, String pair, Integer before, Integer after);

    ExchangeList getExchanges();

    MarketList getMarkets();

}
