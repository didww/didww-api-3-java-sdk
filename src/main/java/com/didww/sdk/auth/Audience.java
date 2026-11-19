package com.didww.sdk.auth;

/**
 * The service a request is authenticated for. Requests carry it as an OkHttp
 * tag ({@code Request.Builder.tag(Audience.class, audience)}); a request
 * without the tag is a JSON:API request for {@link #API}, or for no token when
 * it names a public resource ({@code public_keys}).
 *
 * <p>The values are the production host names of the services, the same in
 * every environment and with any overridden service URL.
 */
public final class Audience {
    /** The JSON:API, export downloads and encrypted file uploads. */
    public static final Audience API = new Audience("api.didww.com");
    /** Voice IN prices. */
    public static final Audience VOICE_IN = new Audience("api-vin.didww.com");
    /** Voice OUT prices. */
    public static final Audience VOICE_OUT = new Audience("api-vout.didww.com");
    /** SMS prices. */
    public static final Audience SMS = new Audience("api-sms.didww.com");
    /** No token: the request is sent without {@code Authorization}. */
    public static final Audience NONE = new Audience(null);

    private final String value;

    private Audience(String value) {
        this.value = value;
    }

    /**
     * Returns the audience requested from the token service.
     *
     * @return the service host name, or {@code null} for {@link #NONE}
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value != null ? value : "none";
    }
}
