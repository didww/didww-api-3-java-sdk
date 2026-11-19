package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body encoding of an {@code http_in} SMS trunk configuration. */
public enum SmsBodyType {
    @JsonProperty("json") JSON,
    @JsonProperty("html_multipart") HTML_MULTIPART,
    @JsonProperty("html_url_encoded") HTML_URL_ENCODED,
    @JsonProperty("raw") RAW;
}
