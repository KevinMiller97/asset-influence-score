package com.millerk97.ais.cryptowatch.exception;

import com.millerk97.ais.cryptowatch.CryptowatchApiError;

public class CryptowatchApiException extends RuntimeException {
    private CryptowatchApiError error;

    public CryptowatchApiException(CryptowatchApiError error) {
        this.error = error;
    }

    public CryptowatchApiException(Throwable cause) {
        super(cause);
    }

    public CryptowatchApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptowatchApiError getError() {
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