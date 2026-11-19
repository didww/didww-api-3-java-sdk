package com.didww.sdk;

import com.didww.sdk.internal.Redact;

import java.util.Objects;

/**
 * The API key and the environment of a client. {@link #toString()} never shows
 * the API key.
 */
public class DidwwCredentials {
    private final String apiKey;
    private final DidwwEnvironment environment;

    public DidwwCredentials(String apiKey, DidwwEnvironment environment) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
    }

    public String getApiKey() {
        return apiKey;
    }

    public DidwwEnvironment getEnvironment() {
        return environment;
    }

    public String getBaseUrl() {
        return environment.getBaseUrl();
    }

    @Override
    public String toString() {
        return "DidwwCredentials{apiKey=" + Redact.mask(apiKey) + ", environment=" + environment + "}";
    }
}
