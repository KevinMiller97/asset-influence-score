package com.millerk97.ais.cryptowatch;

import com.millerk97.ais.cryptowatch.domain.Exchange.ExchangeList;
import com.millerk97.ais.cryptowatch.domain.market.MarketList;
import com.millerk97.ais.cryptowatch.domain.ohlc.OHLCResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CryptowatchApiService {

    /*TODO make periods dynamic, start with just daily here*/
    @GET("markets/{exchange}/{pair}/ohlc?periods=86400")
    Call<OHLCResult> getDailyOHLC(@Path("exchange") String exchange, @Path("pair") String pair, @Query("before") Integer before, @Query("after") Integer after);

    @GET("markets/{exchange}/{pair}/ohlc?periods=3600")
    Call<OHLCResult> getHourlyOHLC(@Path("exchange") String exchange, @Path("pair") String pair, @Query("before") Integer before, @Query("after") Integer after);

    @GET("exchanges")
    Call<ExchangeList> getExchanges();

    @GET("markets")
    Call<MarketList> getMarkets();
}
