package com.millerk97.ais.coingecko;

import com.millerk97.ais.coingecko.coins.CoinFullData;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangeById;
import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesList;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesTickersById;
import com.millerk97.ais.coingecko.domain.Ping;
import com.millerk97.ais.coingecko.global.Global;

import java.util.List;
import java.util.Map;

public interface CoinGeckoApiClient {
    Ping ping();

    List<Exchanges> getExchanges();

    List<Exchanges> getExchanges(int perPage, int page);

    List<ExchangesList> getExchangesList();

    Map<String, Map<String, Double>> getPrice(String ids, String vsCurrencies, boolean includeMarketCap, boolean include24hrVol,
                                              boolean include24hrChange, boolean includeLastUpdatedAt);

    Global getGlobal();

    ExchangeById getExchangesById(String id);

    ExchangesTickersById getExchangesTickersById(String id);

    ExchangesTickersById getExchangesTickersById(String id, String coinIds, Integer page, String order);

    List<List<String>> getExchangesVolumeChart(String id, Integer days);

    ExchangesTickersById getTickers(String id, int page);

    CoinFullData getCoinById(String id);

    void shutdown();
}
