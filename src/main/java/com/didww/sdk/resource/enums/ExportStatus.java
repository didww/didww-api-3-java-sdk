package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExportStatus {
    @JsonProperty("Pending") PENDING,
    @JsonProperty("Processing") PROCESSING,
    @JsonProperty("Completed") COMPLETED;
}
