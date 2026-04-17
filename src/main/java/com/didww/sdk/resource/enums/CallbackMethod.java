package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CallbackMethod {
    @JsonProperty("post") POST,
    @JsonProperty("get") GET;
}
