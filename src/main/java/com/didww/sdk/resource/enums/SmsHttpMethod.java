package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** HTTP method of an {@code http_in} SMS trunk configuration. */
public enum SmsHttpMethod {
    @JsonProperty("get") GET,
    @JsonProperty("post") POST,
    @JsonProperty("put") PUT;
}
