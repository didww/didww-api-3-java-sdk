package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OrderStatus {
    @JsonProperty("pending") PENDING,
    @JsonProperty("canceled") CANCELED,
    @JsonProperty("completed") COMPLETED;
}
