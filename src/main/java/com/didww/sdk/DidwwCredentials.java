package com.didww.sdk;

import java.util.Objects;

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
}
