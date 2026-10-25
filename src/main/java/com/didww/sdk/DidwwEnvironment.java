package com.didww.sdk;

/**
 * A DIDWW API environment with the URLs of its services.
 */
public enum DidwwEnvironment {
    PRODUCTION("https://api.didww.com/v3",
            "https://auth.didww.com",
            "https://api-vin.didww.com/v3",
            "https://api-vout.didww.com/v3",
            "https://api-sms.didww.com/v3"),
    SANDBOX("https://sandbox-api.didww.com/v3",
            "https://auth-sandbox.didww.com",
            "https://api-vin-sandbox.didww.com/v3",
            "https://api-vout-sandbox.didww.com/v3",
            "https://api-sms-sandbox.didww.com/v3");

    private final String baseUrl;
    private final String authUrl;
    private final String voiceInPricesUrl;
    private final String voiceOutPricesUrl;
    private final String smsPricesUrl;

    DidwwEnvironment(String baseUrl, String authUrl, String voiceInPricesUrl, String voiceOutPricesUrl,
                     String smsPricesUrl) {
        this.baseUrl = baseUrl;
        this.authUrl = authUrl;
        this.voiceInPricesUrl = voiceInPricesUrl;
        this.voiceOutPricesUrl = voiceOutPricesUrl;
        this.smsPricesUrl = smsPricesUrl;
    }

    /**
     * Returns the JSON:API base URL.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the token service base URL.
     *
     * @return the token service URL
     */
    public String getAuthUrl() {
        return authUrl;
    }

    /**
     * Returns the Voice IN prices base URL.
     *
     * @return the Voice IN prices URL
     */
    public String getVoiceInPricesUrl() {
        return voiceInPricesUrl;
    }

    /**
     * Returns the Voice OUT prices base URL.
     *
     * @return the Voice OUT prices URL
     */
    public String getVoiceOutPricesUrl() {
        return voiceOutPricesUrl;
    }

    /**
     * Returns the SMS prices base URL.
     *
     * @return the SMS prices URL
     */
    public String getSmsPricesUrl() {
        return smsPricesUrl;
    }
}
