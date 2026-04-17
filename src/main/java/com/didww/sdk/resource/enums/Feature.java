package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Feature {
    @JsonProperty("voice_in") VOICE_IN,
    @JsonProperty("voice_out") VOICE_OUT,
    @JsonProperty("t38") T38,
    @JsonProperty("sms_in") SMS_IN,
    @JsonProperty("p2p") P2P,
    @JsonProperty("a2p") A2P,
    @JsonProperty("emergency") EMERGENCY,
    @JsonProperty("cnam_out") CNAM_OUT;
}
