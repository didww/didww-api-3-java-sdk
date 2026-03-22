package com.didww.sdk.http;

import com.didww.sdk.SdkVersion;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ApiKeyInterceptor implements Interceptor {
    public static final String API_VERSION_HEADER = "X-DIDWW-API-Version";
    public static final String API_VERSION = "2022-05-10";
    private final String apiKey;

    public ApiKeyInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Adds authentication and versioning headers to every outgoing request.
     *
     * @param chain OkHttp interceptor chain
     * @return response from the next interceptor or the network
     * @throws IOException if the request could not be executed
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Content-Type", JsonApiMediaType.VALUE)
                .header("Accept", JsonApiMediaType.VALUE)
                .header("User-Agent", SdkVersion.userAgent())
                .header(API_VERSION_HEADER, API_VERSION);

        if (!original.url().pathSegments().contains("public_keys")) {
            builder.header("Api-Key", apiKey);
        }

        return chain.proceed(builder.build());
    }
}
