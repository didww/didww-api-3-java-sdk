package com.didww.sdk.exception;

/**
 * The reason why a prices service call failed.
 */
public enum PricesErrorKind {
    /** The request was refused (400), for example a filter value of the wrong format. */
    BAD_REQUEST,

    /** The token was refused again after it was replaced once (401). */
    NOT_AUTHORIZED,

    /** The token does not allow the request (403). It is not retried. */
    ACCESS_DENIED,

    /**
     * The account has no price plan for the service: a 404 other than
     * {@code price_not_found}, including a 404 without a body.
     */
    PRICE_PLAN_NOT_ASSIGNED,

    /** No price matches the lookup (404 {@code price_not_found}). */
    PRICE_NOT_FOUND,

    /** The service cannot answer in the requested format (406). */
    NOT_ACCEPTABLE,

    /**
     * The request budget is spent (429). It is not retried;
     * {@link DidwwPricesException#getRetryAfter()} says when to try again.
     */
    TOO_MANY_REQUESTS,

    /**
     * The service cannot answer yet (503), for example a price file that is
     * still being generated. {@link DidwwPricesException#getRetryAfter()} says
     * when to try again.
     */
    SERVICE_UNAVAILABLE,

    /** A status the call does not expect, such as a redirect, 202, 204 or a 304 to a lookup. */
    UNEXPECTED_STATUS,

    /**
     * The request failed in transport: the service could not be reached, it
     * timed out, TLS failed, the response body broke off or was shorter than
     * its Content-Length, or the calling thread was interrupted (its interrupt
     * flag is then set). It is not retried.
     */
    CONNECTION_ERROR
}
