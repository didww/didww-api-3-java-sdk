package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IdentityType {
    @JsonProperty("Personal") PERSONAL,
    @JsonProperty("Business") BUSINESS,
    @JsonProperty("Any") ANY;
}
