package com.didww.sdk.exception;

import java.time.Duration;

/**
 * Raised when an access token cannot be obtained from the token service. It
 * never contains the API key or a token. Use {@link #getKind()} to tell the
 * cases apart.
 */
public class DidwwAuthException extends DidwwException {
    private final AuthErrorKind kind;
    private final Integer httpStatus;
    private final String error;
    private final String errorDescription;
    private final Duration retryAfter;

    /**
     * Creates the exception.
     *
     * @param kind             the reason of the failure
     * @param httpStatus       HTTP status of the token service, or {@code null} when no answer was received
     * @param error            the {@code error} field of the answer, or {@code null}
     * @param errorDescription the {@code error_description} field of the answer, or {@code null}
     * @param retryAfter       when to try again (with {@link AuthErrorKind#RATE_LIMITED}), or {@code null}
     * @param cause            the transport failure, or {@code null}
     */
    public DidwwAuthException(AuthErrorKind kind, Integer httpStatus, String error, String errorDescription,
                              Duration retryAfter, Throwable cause) {
        super(buildMessage(httpStatus, error, errorDescription, cause), cause);
        this.kind = kind;
        this.httpStatus = httpStatus;
        this.error = error;
        this.errorDescription = errorDescription;
        this.retryAfter = retryAfter;
    }

    /**
     * Returns the reason of the failure.
     *
     * @return the error kind
     */
    public AuthErrorKind getKind() {
        return kind;
    }

    /**
     * Returns the HTTP status of the token service.
     *
     * @return the status, or {@code null} when no answer was received
     */
    public Integer getHttpStatus() {
        return httpStatus;
    }

    /**
     * Returns the {@code error} field of the answer, for example {@code invalid_client}.
     *
     * @return the error code, or {@code null}
     */
    public String getError() {
        return error;
    }

    /**
     * Returns the {@code error_description} field of the answer.
     *
     * @return the description, or {@code null}
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Returns when to try again; set with {@link AuthErrorKind#RATE_LIMITED}.
     *
     * @return the time to wait, or {@code null}
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }

    private static String buildMessage(Integer httpStatus, String error, String errorDescription, Throwable cause) {
        String prefix = "Token service: ";
        boolean hasError = error != null && !error.isEmpty();
        boolean hasDescription = errorDescription != null && !errorDescription.isEmpty();
        if (hasError && hasDescription) {
            return prefix + error + ": " + errorDescription;
        }
        if (hasError) {
            return prefix + error;
        }
        if (hasDescription) {
            return prefix + errorDescription;
        }
        if (cause != null) {
            String causeMessage = cause.getMessage();
            return prefix + (causeMessage != null ? causeMessage : cause.getClass().getName());
        }
        return "Token service responded with status " + httpStatus;
    }
}
