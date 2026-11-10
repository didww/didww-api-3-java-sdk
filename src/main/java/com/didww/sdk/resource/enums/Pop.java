package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** DIDWW point of presence a voice IN trunk is served from (API 2026-10-22). */
public enum Pop {
    @JsonProperty("nyc") NYC,
    @JsonProperty("fra") FRA,
    @JsonProperty("lac") LAC,
    @JsonProperty("mia") MIA,
    @JsonProperty("sg") SG,
    @JsonProperty("hk") HK,
    @JsonProperty("ams") AMS;
}
