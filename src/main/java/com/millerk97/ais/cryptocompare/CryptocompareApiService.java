package com.millerk97.ais.cryptocompare;

import com.millerk97.ais.cryptocompare.domain.ohlc.APIResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CryptocompareApiService {

    @GET("data/v2/histoday")
    Call<APIResult> getDailyOHLC(@Query("e") String exchange, @Query("fsym") String base, @Query("tsym") String target, @Query("limit") Integer limit, @Query("toTs") Integer before);

    @GET("data/v2/histohour")
    Call<APIResult> getHourlyOHLC(@Query("e") String exchange, @Query("fsym") String base, @Query("tsym") String target, @Query("limit") Integer limit, @Query("toTs") Integer before);
}
