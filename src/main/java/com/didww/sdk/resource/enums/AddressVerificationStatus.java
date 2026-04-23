package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AddressVerificationStatus {
    @JsonProperty("pending") PENDING,
    @JsonProperty("approved") APPROVED,
    @JsonProperty("rejected") REJECTED;
}
