package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EmergencyCallingServiceStatus {
    @JsonProperty("active") ACTIVE,
    @JsonProperty("canceled") CANCELED,
    @JsonProperty("changes required") CHANGES_REQUIRED,
    @JsonProperty("in process") IN_PROCESS,
    @JsonProperty("new") NEW,
    @JsonProperty("pending update") PENDING_UPDATE;
}
