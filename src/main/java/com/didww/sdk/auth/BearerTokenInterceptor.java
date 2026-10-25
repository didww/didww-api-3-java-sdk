package com.didww.sdk.auth;

import com.didww.sdk.SdkVersion;
import com.didww.sdk.http.JsonApiMediaType;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Internal, not part of the SDK API: authenticates every request with a bearer
 * token for its {@link Audience}. The API key is never sent here: it goes to
 * the token service only.
 *
 * <p>A 401 answer evicts the refused token and the request is sent once more
 * with a newly minted one, unless its body is one-shot. A second 401 evicts the
 * replacement too and is returned. A 403 is never retried. Token errors
 * ({@link com.didww.sdk.exception.DidwwAuthException}) and transport errors
 * propagate unchanged.
 *
 * <p>Requests without an {@link Audience} tag are JSON:API requests: they get
 * {@code Accept: application/vnd.api+json} when they have no {@code Accept},
 * and no token when the path below {@code /v3} names a public resource
 * ({@code public_keys}). {@code If-None-Match} is never sent, as the services
 * would ignore {@code If-Modified-Since} next to it.
 */
public final class BearerTokenInterceptor implements Interceptor {
    private final TokenProvider tokens;

    /**
     * Creates the interceptor.
     *
     * @param tokens the token provider
     */
    public BearerTokenInterceptor(TokenProvider tokens) {
        this.tokens = Objects.requireNonNull(tokens, "tokens must not be null");
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Audience tagged = original.tag(Audience.class);
        Request.Builder builder = original.newBuilder().removeHeader("If-None-Match");
        if (original.header("User-Agent") == null) {
            builder.header("User-Agent", SdkVersion.userAgent());
        }

        String audience;
        if (tagged != null) {
            audience = tagged.getValue();
        } else {
            if (original.header("Accept") == null) {
                builder.header("Accept", JsonApiMediaType.VALUE);
            }
            audience = isPublicEndpoint(original.url()) ? null : Audience.API.getValue();
        }
        if (audience == null) {
            return chain.proceed(builder.removeHeader("Authorization").build());
        }

        AccessToken token = tokens.token(audience);
        Response response = chain.proceed(builder.header("Authorization", "Bearer " + token.getAccessToken()).build());
        if (response.code() != 401) {
            return response;
        }

        tokens.invalidate(audience, token.getAccessToken());
        RequestBody body = original.body();
        if (body != null && body.isOneShot()) {
            return response;
        }
        response.close();

        token = tokens.token(audience);
        response = chain.proceed(builder.header("Authorization", "Bearer " + token.getAccessToken()).build());
        if (response.code() == 401) {
            tokens.invalidate(audience, token.getAccessToken());
        }
        return response;
    }

    // Whether the JSON:API path names a public resource (the segment after
    // v3), which is called without a token.
    static boolean isPublicEndpoint(HttpUrl url) {
        List<String> segments = url.pathSegments();
        int v3 = segments.indexOf("v3");
        return v3 >= 0 && v3 + 1 < segments.size() && "public_keys".equals(segments.get(v3 + 1));
    }
}
