package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ICE (Interactive Connectivity Establishment) on the calls of a trunk: not used, accepted when the
 * other side offers it, or initiated by DIDWW. (API 2026-10-22)
 */
public enum IceMode {
    @JsonProperty("disabled") DISABLED,
    @JsonProperty("accept") ACCEPT,
    @JsonProperty("initiate") INITIATE;
}
