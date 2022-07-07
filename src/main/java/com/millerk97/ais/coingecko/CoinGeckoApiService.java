package com.millerk97.ais.coingecko;

import com.millerk97.ais.coingecko.coins.CoinFullData;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangeById;
import com.millerk97.ais.coingecko.domain.Exchanges.Exchanges;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesList;
import com.millerk97.ais.coingecko.domain.Exchanges.ExchangesTickersById;
import com.millerk97.ais.coingecko.domain.Ping;
import com.millerk97.ais.coingecko.global.Global;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

public interface CoinGeckoApiService {
    @GET("ping")
    Call<Ping> ping();

    @GET("exchanges")
    Call<List<Exchanges>> getExchanges(@Query("per_page") int perPage, @Query("page") int page);

    @GET("simple/price")
    Call<Map<String, Map<String, Double>>> getPrice(@Query("ids") String ids,
                                                    @Query("vs_currencies") String vsCurrencies,
                                                    @Query("include_market_cap") boolean includeMarketCap,
                                                    @Query("include_24hr_vol") boolean include24hrVol,
                                                    @Query("include_24hr_change") boolean include24hrChange,
                                                    @Query("include_last_updated_at") boolean includeLastUpdatedAt);

    @GET("global")
    Call<Global> getGlobal();

    @GET("exchanges/list")
    Call<List<ExchangesList>> getExchangesList();

    @GET("exchanges/{id}")
    Call<ExchangeById> getExchangesById(@Path("id") String id);

    @GET("exchanges/{id}/tickers")
    Call<ExchangesTickersById> getExchangesTickersById(@Path("id") String id, @Query("coin_ids") String coinIds,
                                                       @Query("page") Integer page, @Query("order") String order);

    @GET("exchanges/{id}/volume_chart")
    Call<List<List<String>>> getExchangesVolumeChart(@Path("id") String id, @Query("days") Integer days);

    /* skip all optional flags, we only want FullData */
    @GET("coins/{id}?localization=false&market_data=false&community_data=false&developer_data=false&sparkline=false")
    Call<CoinFullData> getCoinById(@Path("id") String id);

    @GET("coins/{id}/tickers")
    Call<ExchangesTickersById> getTickers(@Path("id") String id, @Query("page") int page);
}
