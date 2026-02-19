package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum VoiceOutTrunkStatus {
    @JsonProperty("active") ACTIVE,
    @JsonProperty("blocked") BLOCKED;
}
