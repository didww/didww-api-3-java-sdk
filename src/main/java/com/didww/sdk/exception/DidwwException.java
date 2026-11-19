package com.didww.sdk.exception;

/**
 * Base class of every exception raised by the SDK. Messages never contain the
 * API key or an access token.
 */
public abstract class DidwwException extends RuntimeException {

    protected DidwwException(String message) {
        super(message);
    }

    protected DidwwException(String message, Throwable cause) {
        super(message, cause);
    }
}
