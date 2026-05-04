package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NetworkProtocolPriority {
    @JsonProperty("force_ipv4") FORCE_IPV4,
    @JsonProperty("force_ipv6") FORCE_IPV6,
    @JsonProperty("any") ANY,
    @JsonProperty("prefer_ipv4") PREFER_IPV4,
    @JsonProperty("prefer_ipv6") PREFER_IPV6;
}
