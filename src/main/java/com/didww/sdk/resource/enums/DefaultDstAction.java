package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DefaultDstAction {
    @JsonProperty("allow_all") ALLOW_ALL,
    @JsonProperty("reject_all") REJECT_ALL;
}
