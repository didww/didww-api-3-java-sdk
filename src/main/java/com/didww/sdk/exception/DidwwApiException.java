package com.didww.sdk.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An error status of the JSON:API repositories, the export downloads or
 * {@code uploadEncryptedFile}, with the error objects of the answer. When the
 * body is not a JSON:API errors document, the export downloads and the upload
 * report one error object holding the raw body, and the repositories give the
 * raw body as the message, without error objects.
 *
 * <p>A 429 ({@code too_many_requests}) means the request budget of the account
 * for the current window is spent. The SDK does not retry it, as a write may
 * not be safe to send twice: wait {@link #getRetryAfter()} before sending the
 * request again.
 */
public class DidwwApiException extends DidwwException {
    private final int httpStatus;
    private final transient List<ApiError> errors;
    private final Duration retryAfter;

    public DidwwApiException(int httpStatus, List<ApiError> errors) {
        this(httpStatus, errors, null);
    }

    /**
     * Creates the exception.
     *
     * @param httpStatus HTTP status of the answer
     * @param errors     the error objects of the answer, or {@code null}
     * @param retryAfter the Retry-After of the answer, when given in seconds, or {@code null}
     */
    public DidwwApiException(int httpStatus, List<ApiError> errors, Duration retryAfter) {
        super(buildMessage(httpStatus, errors));
        this.httpStatus = httpStatus;
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
        this.retryAfter = retryAfter;
    }

    public DidwwApiException(int httpStatus, String message) {
        this(httpStatus, message, null);
    }

    /**
     * Creates the exception for an answer without error objects.
     *
     * @param httpStatus HTTP status of the answer
     * @param message    the message
     * @param retryAfter the Retry-After of the answer, when given in seconds, or {@code null}
     */
    public DidwwApiException(int httpStatus, String message, Duration retryAfter) {
        super(message);
        this.httpStatus = httpStatus;
        this.errors = Collections.emptyList();
        this.retryAfter = retryAfter;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public List<ApiError> getErrors() {
        return errors != null ? errors : Collections.emptyList();
    }

    /**
     * Returns the Retry-After of the answer, when it is given in seconds, for
     * any error status; a 429 carries it.
     *
     * @return the time to wait, or {@code null} when the answer has none or it
     *         is not a number of seconds
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }

    private static String buildMessage(int httpStatus, List<ApiError> errors) {
        StringBuilder sb = new StringBuilder("DIDWW API error (HTTP " + httpStatus + ")");
        if (errors != null && !errors.isEmpty()) {
            StringBuilder details = new StringBuilder();
            for (ApiError error : errors) {
                String msg = error.getMessage();
                if (msg != null && !msg.isBlank()) {
                    if (details.length() > 0) details.append("; ");
                    details.append(msg);
                }
            }
            if (details.length() > 0) {
                sb.append(": ").append(details);
            }
        }
        return sb.toString();
    }

    public static class ApiError {
        private String title;
        private String detail;
        private String status;
        private String code;
        private Map<String, Object> source;
        private Map<String, Object> meta;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public Map<String, Object> getSource() { return source; }
        public void setSource(Map<String, Object> source) { this.source = source; }

        public Map<String, Object> getMeta() { return meta; }
        public void setMeta(Map<String, Object> meta) { this.meta = meta; }

        /**
         * Returns {@code detail} if present, otherwise falls back to {@code title},
         * or {@code "Unknown error"} when both are absent.
         *
         * @return human-readable error message
         */
        @JsonIgnore
        public String getMessage() {
            if (detail != null) return detail;
            if (title != null) return title;
            return "Unknown error";
        }

        @Override
        public String toString() {
            return "ApiError{title='" + title + "', detail='" + detail + "'}";
        }
    }
}
