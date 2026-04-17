package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IdentityType {
    @JsonProperty("personal") PERSONAL,
    @JsonProperty("business") BUSINESS,
    @JsonProperty("any") ANY;
}
