package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Codec of a Voice OUT trunk ({@code VoiceOutTrunk#setCodecs}). An inbound SIP trunk has its own
 * list, {@link Codec}. (API 2026-10-22)
 */
public enum VoiceOutCodec {
    @JsonProperty("telephone_event") TELEPHONE_EVENT,
    @JsonProperty("g723") G723,
    @JsonProperty("g729") G729,
    @JsonProperty("pcmu") PCMU,
    @JsonProperty("pcma") PCMA,
    @JsonProperty("gsm") GSM,
    @JsonProperty("g722") G722,
    @JsonProperty("opus") OPUS,
    @JsonProperty("amr") AMR,
    @JsonProperty("amr_wb") AMR_WB;
}
