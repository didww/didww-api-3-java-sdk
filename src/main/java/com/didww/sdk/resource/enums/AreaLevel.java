package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AreaLevel {
    @JsonProperty("world_wide") WORLDWIDE,
    @JsonProperty("country") COUNTRY,
    @JsonProperty("area") AREA,
    @JsonProperty("city") CITY;
}
