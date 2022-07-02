package com.millerk97.ais.cryptowatch;

import com.millerk97.ais.cryptowatch.exception.CryptowatchApiException;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

public class CryptowatchApi {
    private final String API_BASE_URL = "https://api.cryptowat.ch/";

    private OkHttpClient okHttpClient = null;
    private Retrofit retrofit = null;

    public <S> S createService(Class<S> serviceClass, Long connectionTimeoutSeconds, Long readTimeoutSeconds, Long writeTimeoutSeconds) {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return retrofit.create(serviceClass);
    }

    public <T> T executeSync(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                try {
                    CryptowatchApiError apiError = getCoinGeckoApiError(response);
                    apiError.setCode(response.code());
                    throw new CryptowatchApiException(apiError);
                } catch (IOException e) {
                    throw new CryptowatchApiException(response.toString(), e);
                }
            }
        } catch (IOException e) {
            throw new CryptowatchApiException(e);
        }
    }

    public void shutdown() {
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
    }

    private CryptowatchApiError getCoinGeckoApiError(Response<?> response) throws IOException {
        return (CryptowatchApiError) retrofit.responseBodyConverter(CryptowatchApiError.class, new Annotation[0])
                .convert(response.errorBody());
    }
}
