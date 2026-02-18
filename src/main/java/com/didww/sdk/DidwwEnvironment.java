package com.didww.sdk;

public enum DidwwEnvironment {
    PRODUCTION("https://api.didww.com/v3"),
    SANDBOX("https://sandbox-api.didww.com/v3");

    private final String baseUrl;

    DidwwEnvironment(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
