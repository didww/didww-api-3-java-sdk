package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AreaLevel {
    @JsonProperty("WorldWide") WORLDWIDE,
    @JsonProperty("Country") COUNTRY,
    @JsonProperty("Area") AREA,
    @JsonProperty("City") CITY;
}
