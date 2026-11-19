package com.didww.sdk.exception;

/**
 * The reason why an access token could not be obtained.
 */
public enum AuthErrorKind {
    /** The token service refused the API key (401). It is not retried. */
    INVALID_CLIENT,

    /** The token service rejected the request (400), for example an unknown scope. */
    INVALID_REQUEST,

    /**
     * The token service answered 429 (too many token requests). Further token
     * requests of the same API key and token service fail without a request
     * until {@link DidwwAuthException#getRetryAfter()} plus a small random delay
     * has passed; such a refusal reports the time still left, so retry in a loop
     * rather than once.
     */
    RATE_LIMITED,

    /** The last attempt to reach the token service was answered with a 5xx status. */
    SERVER_ERROR,

    /**
     * The token request could not be sent or answered: the token service could
     * not be reached, the attempts timed out, TLS failed, or the calling thread
     * was interrupted.
     */
    CONNECTION_ERROR,

    /** The granted token is empty, not a valid header value, or does not cover the requested audience. */
    INVALID_GRANT,

    /** The token service answered with a status the SDK does not expect, such as a redirect. */
    UNEXPECTED_STATUS
}
