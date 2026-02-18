package com.didww.sdk.http;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ApiKeyInterceptor implements Interceptor {
    private final String apiKey;

    public ApiKeyInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
                .header("Api-Key", apiKey)
                .header("Content-Type", JsonApiMediaType.VALUE)
                .header("Accept", JsonApiMediaType.VALUE)
                .header("User-Agent", "didww-java-sdk/1.0.0")
                .build();
        return chain.proceed(request);
    }
}
