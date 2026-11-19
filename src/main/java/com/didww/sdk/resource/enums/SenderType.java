package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SenderType {
    @JsonProperty("alphanumeric") ALPHANUMERIC,
    @JsonProperty("carrier") CARRIER,
    @JsonProperty("long_code") LONG_CODE,
    @JsonProperty("short_code") SHORT_CODE,
    @JsonProperty("toll_free") TOLL_FREE;
}
