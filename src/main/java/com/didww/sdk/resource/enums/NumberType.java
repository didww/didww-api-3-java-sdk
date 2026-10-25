package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Number type of a DID group and of the requirements bound to it (API 2026-10-22). */
public enum NumberType {
    @JsonProperty("local") LOCAL,
    @JsonProperty("national") NATIONAL,
    @JsonProperty("mobile") MOBILE,
    @JsonProperty("toll_free") TOLL_FREE,
    @JsonProperty("shared_cost") SHARED_COST,
    @JsonProperty("global") GLOBAL;
}
