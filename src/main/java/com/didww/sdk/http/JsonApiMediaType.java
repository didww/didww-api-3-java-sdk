package com.didww.sdk.http;

import okhttp3.MediaType;

public final class JsonApiMediaType {
    public static final String VALUE = "application/vnd.api+json";
    public static final MediaType MEDIA_TYPE = MediaType.parse(VALUE);

    private JsonApiMediaType() {
    }
}
