package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SenderIdVerificationStatus {
    @JsonProperty("active") ACTIVE,
    @JsonProperty("changes_required") CHANGES_REQUIRED,
    @JsonProperty("closed") CLOSED,
    @JsonProperty("closing_in_process") CLOSING_IN_PROCESS,
    @JsonProperty("in_process") IN_PROCESS,
    @JsonProperty("new") NEW;
}
