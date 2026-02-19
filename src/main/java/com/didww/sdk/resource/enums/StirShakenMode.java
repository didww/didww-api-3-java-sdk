package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StirShakenMode {
    @JsonProperty("disabled") DISABLED,
    @JsonProperty("original") ORIGINAL,
    @JsonProperty("pai") PAI,
    @JsonProperty("original_pai") ORIGINAL_PAI,
    @JsonProperty("verstat") VERSTAT;
}
