package com.didww.sdk.internal;

import okhttp3.Response;

import java.time.Duration;

/**
 * Internal helper, not part of the SDK API: reads the {@code Retry-After} of
 * an answer.
 */
public final class RetryAfter {

    private RetryAfter() {
    }

    /**
     * Returns the {@code Retry-After} of {@code response} when it is a whole
     * number of seconds; an HTTP date or any other value reads as {@code null}.
     *
     * @param response the answer
     * @return the time to wait, or {@code null}
     */
    public static Duration seconds(Response response) {
        String header = response.header("Retry-After");
        if (header == null) {
            return null;
        }
        String value = header.trim();
        return value.matches("\\d{1,9}") ? Duration.ofSeconds(Long.parseLong(value)) : null;
    }
}
