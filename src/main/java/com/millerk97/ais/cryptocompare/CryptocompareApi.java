package com.millerk97.ais.cryptocompare;

import com.millerk97.ais.cryptocompare.exception.CryptowatchApiException;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

public class CryptocompareApi {
    private final String API_BASE_URL = "https://min-api.cryptocompare.com/";

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
                    CryptocompareApiError apiError = getCryptocompareApiError(response);
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

    private CryptocompareApiError getCryptocompareApiError(Response<?> response) throws IOException {
        return (CryptocompareApiError) retrofit.responseBodyConverter(CryptocompareApiError.class, new Annotation[0])
                .convert(response.errorBody());
    }
}
