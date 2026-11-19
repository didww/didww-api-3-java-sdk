package com.didww.sdk.auth;

import com.didww.sdk.internal.Redact;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An access token granted by the token service.
 *
 * <p>It is immutable. It serializes with Jackson under the wire names
 * ({@code access_token}, {@code token_type}, {@code expires_in}, {@code aud},
 * {@code scope}, {@code api_version}, {@code minted_at} as an ISO-8601 string),
 * the access token included, so that a {@link TokenStore} can persist it;
 * {@link #toString()} never shows the access token.
 */
public final class AccessToken {
    private final String accessToken;
    private final String tokenType;
    private final int expiresIn;
    private final List<String> audience;
    private final String scope;
    private final String apiVersion;
    private final Instant mintedAt;

    /**
     * Creates a token. {@code null} strings become empty, a {@code null}
     * audience becomes empty, and a {@code null} {@code mintedAt} becomes now.
     *
     * @param accessToken the bearer token
     * @param tokenType   the token type, {@code Bearer}
     * @param expiresIn   the lifetime in seconds
     * @param audience    the audiences the token is valid for
     * @param scope       the granted scope
     * @param apiVersion  the granted API version
     * @param mintedAt    when the SDK received the token
     */
    public AccessToken(String accessToken, String tokenType, int expiresIn, List<String> audience,
                       String scope, String apiVersion, Instant mintedAt) {
        this.accessToken = accessToken != null ? accessToken : "";
        this.tokenType = tokenType != null ? tokenType : "";
        this.expiresIn = expiresIn;
        this.audience = audience != null
                ? Collections.unmodifiableList(new ArrayList<>(audience))
                : Collections.emptyList();
        this.scope = scope != null ? scope : "";
        this.apiVersion = apiVersion != null ? apiVersion : "";
        this.mintedAt = mintedAt != null ? mintedAt : Instant.now();
    }

    @JsonCreator
    private static AccessToken fromJson(@JsonProperty("access_token") String accessToken,
                                        @JsonProperty("token_type") String tokenType,
                                        @JsonProperty("expires_in") int expiresIn,
                                        @JsonProperty("aud")
                                        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                        List<String> audience,
                                        @JsonProperty("scope") String scope,
                                        @JsonProperty("api_version") String apiVersion,
                                        @JsonProperty("minted_at") String mintedAt) {
        Instant minted = null;
        if (mintedAt != null) {
            try {
                minted = Instant.parse(mintedAt);
            } catch (DateTimeParseException ignored) {
                // An unreadable minted_at becomes now.
            }
        }
        return new AccessToken(accessToken, tokenType, expiresIn, audience, scope, apiVersion, minted);
    }

    /**
     * Returns the bearer token. Treat it like the API key.
     *
     * @return the access token
     */
    @JsonProperty("access_token")
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Returns the token type.
     *
     * @return {@code Bearer}
     */
    @JsonProperty("token_type")
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns the lifetime of the token in seconds.
     *
     * @return the lifetime
     */
    @JsonProperty("expires_in")
    public int getExpiresIn() {
        return expiresIn;
    }

    /**
     * Returns the audiences (service host names) the token is valid for.
     *
     * @return an unmodifiable list
     */
    @JsonProperty("aud")
    public List<String> getAudience() {
        return audience;
    }

    /**
     * Returns the granted scope.
     *
     * @return {@code read}, {@code full} or empty
     */
    @JsonProperty("scope")
    public String getScope() {
        return scope;
    }

    /**
     * Returns the granted API version.
     *
     * @return the API version
     */
    @JsonProperty("api_version")
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Returns when the SDK received the token.
     *
     * @return the mint time
     */
    @JsonIgnore
    public Instant getMintedAt() {
        return mintedAt;
    }

    @JsonProperty("minted_at")
    private String mintedAtJson() {
        return mintedAt.toString();
    }

    /**
     * Describes the token without the access token.
     *
     * @return the description
     */
    @Override
    public String toString() {
        return "AccessToken{accessToken=" + Redact.mask(accessToken.isEmpty() ? null : accessToken)
                + ", tokenType=" + tokenType
                + ", expiresIn=" + expiresIn
                + ", audience=" + audience
                + ", scope=" + scope
                + ", apiVersion=" + apiVersion
                + ", mintedAt=" + mintedAt + "}";
    }
}
