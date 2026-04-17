package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExportStatus {
    @JsonProperty("pending") PENDING,
    @JsonProperty("processing") PROCESSING,
    @JsonProperty("completed") COMPLETED;
}
