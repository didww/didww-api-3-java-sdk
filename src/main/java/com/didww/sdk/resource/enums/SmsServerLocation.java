package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Location of the DIDWW SMPP server. Production accepts both; the sandbox accepts {@link #NYC} only. */
public enum SmsServerLocation {
    @JsonProperty("nyc") NYC,
    @JsonProperty("ams") AMS;
}
