package com.millerk97.ais.twitter.api;


public class TwitterApiException extends RuntimeException {

    private TwitterApiError error;

    public TwitterApiException(TwitterApiError error) {
        this.error = error;
    }

    public TwitterApiException(Throwable cause) {
        super(cause);
    }

    public TwitterApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public TwitterApiError getError() {
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
