package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DiversionInjectMode {
    @JsonProperty("none") NONE,
    @JsonProperty("did_number") DID_NUMBER;
}
