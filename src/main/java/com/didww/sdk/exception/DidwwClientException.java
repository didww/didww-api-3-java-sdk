package com.didww.sdk.exception;

public class DidwwClientException extends RuntimeException {

    public DidwwClientException(String message) {
        super(message);
    }

    public DidwwClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
