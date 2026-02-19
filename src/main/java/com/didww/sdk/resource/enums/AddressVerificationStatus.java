package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AddressVerificationStatus {
    @JsonProperty("Pending") PENDING,
    @JsonProperty("Approved") APPROVED,
    @JsonProperty("Rejected") REJECTED;
}
