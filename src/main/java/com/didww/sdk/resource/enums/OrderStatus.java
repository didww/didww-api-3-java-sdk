package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OrderStatus {
    @JsonProperty("Pending") PENDING,
    @JsonProperty("Canceled") CANCELED,
    @JsonProperty("Completed") COMPLETED;
}
