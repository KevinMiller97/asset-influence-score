package com.millerk97.ais.cryptowatch;

import com.millerk97.ais.cryptowatch.domain.Exchange.ExchangeList;
import com.millerk97.ais.cryptowatch.domain.market.MarketList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CryptowatchApiService {

    @GET("markets/{exchange}/{pair}/ohlc")
    Call<double[]> getOHLC(@Path("exchange") String exchange, @Path("pair") String pair);

    /*TODO make periods dynamic, start with just daily here*/
    @GET("markets/{exchange}/{pair}/ohlc/periods=86400")
    Call<double[]> getOHLC(@Path("exchange") String exchange, @Path("pair") String pair, @Query("before") Integer before, @Query("after") Integer after);

    @GET("exchanges")
    Call<ExchangeList> getExchanges();

    @GET("markets")
    Call<MarketList> getMarkets();
}
