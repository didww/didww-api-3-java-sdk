package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EmergencyCallingServiceStatus {
    @JsonProperty("active") ACTIVE,
    @JsonProperty("canceled") CANCELED,
    @JsonProperty("changes_required") CHANGES_REQUIRED,
    @JsonProperty("in_process") IN_PROCESS,
    @JsonProperty("new") NEW,
    @JsonProperty("pending_update") PENDING_UPDATE;
}
