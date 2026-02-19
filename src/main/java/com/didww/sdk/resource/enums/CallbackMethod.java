package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CallbackMethod {
    @JsonProperty("POST") POST,
    @JsonProperty("GET") GET;
}
