package com.millerk97.ais.cryptocompare.exception;

import com.millerk97.ais.cryptocompare.CryptocompareApiError;

public class CryptowatchApiException extends RuntimeException {
    private CryptocompareApiError error;

    public CryptowatchApiException(CryptocompareApiError error) {
        this.error = error;
    }

    public CryptowatchApiException(Throwable cause) {
        super(cause);
    }

    public CryptowatchApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptocompareApiError getError() {
        return error;
    }

    @Override
    public String getMessage() {
        if (error != null) {
            return error.toString();
        }
        return super.getMessage();
    }
}