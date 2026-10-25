package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** DIDWW point of presence a voice IN trunk is served from (API 2026-10-22). */
public enum Pop {
    @JsonProperty("NYC") NYC,
    @JsonProperty("FRA") FRA,
    @JsonProperty("LAC") LAC,
    @JsonProperty("MIA") MIA,
    @JsonProperty("SG") SG,
    @JsonProperty("HK") HK,
    @JsonProperty("AMS") AMS;
}
