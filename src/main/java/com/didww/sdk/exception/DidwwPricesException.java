package com.didww.sdk.exception;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Raised by the prices services. Use {@link #getKind()} to tell the cases
 * apart. It never contains the API key or a token.
 */
public class DidwwPricesException extends DidwwException {
    private final PricesErrorKind kind;
    private final Integer httpStatus;
    private final String code;
    private final List<DidwwApiException.ApiError> errors;
    private final Duration retryAfter;

    /**
     * Creates the exception.
     *
     * @param kind       the reason of the failure
     * @param httpStatus HTTP status of the answer, or {@code null} when no answer was received
     * @param code       the code of the first error object, or {@code null}
     * @param errors     the error objects of the answer, or {@code null}
     * @param retryAfter the Retry-After of the answer, when given in seconds, or {@code null}
     * @param cause      the transport failure, or {@code null}
     */
    public DidwwPricesException(PricesErrorKind kind, Integer httpStatus, String code,
                                List<DidwwApiException.ApiError> errors, Duration retryAfter, Throwable cause) {
        super(buildMessage(httpStatus, errors, cause), cause);
        this.kind = kind;
        this.httpStatus = httpStatus;
        this.code = code;
        this.errors = errors != null ? Collections.unmodifiableList(new ArrayList<>(errors)) : Collections.emptyList();
        this.retryAfter = retryAfter;
    }

    /**
     * Returns the reason of the failure.
     *
     * @return the error kind
     */
    public PricesErrorKind getKind() {
        return kind;
    }

    /**
     * Returns the HTTP status of the answer.
     *
     * @return the status, or {@code null} when no answer was received
     */
    public Integer getHttpStatus() {
        return httpStatus;
    }

    /**
     * Returns the code of the first error object, for example
     * {@code price_not_found}. A 404 without one reports
     * {@code price_plan_not_assigned}.
     *
     * @return the code, or {@code null}
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the error objects of the answer.
     *
     * @return the errors, empty when the answer has none
     */
    public List<DidwwApiException.ApiError> getErrors() {
        return errors;
    }

    /**
     * Returns the Retry-After of the answer, when it is given in seconds; set
     * with {@link PricesErrorKind#TOO_MANY_REQUESTS} and
     * {@link PricesErrorKind#SERVICE_UNAVAILABLE} when the service sends it.
     *
     * @return the time to wait, or {@code null}
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }

    private static String buildMessage(Integer httpStatus, List<DidwwApiException.ApiError> errors, Throwable cause) {
        StringBuilder details = new StringBuilder();
        if (errors != null) {
            for (DidwwApiException.ApiError error : errors) {
                String text = notEmpty(error.getDetail()) ? error.getDetail()
                        : notEmpty(error.getTitle()) ? error.getTitle() : null;
                if (text != null) {
                    if (details.length() > 0) {
                        details.append("; ");
                    }
                    details.append(text);
                }
            }
        }
        if (details.length() > 0) {
            return "Prices service: " + details;
        }
        if (cause != null) {
            String causeMessage = cause.getMessage();
            return "Prices service: " + (causeMessage != null ? causeMessage : cause.getClass().getName());
        }
        if (httpStatus != null) {
            return "Prices service responded with status " + httpStatus;
        }
        return "Prices service request failed";
    }

    private static boolean notEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}
