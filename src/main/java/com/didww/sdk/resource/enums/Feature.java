package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Feature {
    @JsonProperty("voice") VOICE,
    @JsonProperty("voice_in") VOICE_IN,
    @JsonProperty("voice_out") VOICE_OUT,
    @JsonProperty("t38") T38,
    @JsonProperty("sms") SMS,
    @JsonProperty("sms_in") SMS_IN,
    @JsonProperty("sms_out") SMS_OUT;
}
