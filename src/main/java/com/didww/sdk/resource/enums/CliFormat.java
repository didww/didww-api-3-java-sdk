package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CliFormat {
    @JsonProperty("raw") RAW,
    @JsonProperty("e164") E164,
    @JsonProperty("local") LOCAL;
}
