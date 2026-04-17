package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DiversionRelayPolicy {
    @JsonProperty("none") NONE,
    @JsonProperty("as_is") AS_IS,
    @JsonProperty("sip") SIP,
    @JsonProperty("tel") TEL;
}
