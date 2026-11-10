package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Codec of an inbound SIP trunk ({@code SipConfiguration#setCodecs}). A Voice OUT trunk has its own
 * list, {@link VoiceOutCodec}. (API 2026-10-22)
 */
public enum Codec {
    @JsonProperty("telephone_event") TELEPHONE_EVENT,
    @JsonProperty("g723") G723,
    @JsonProperty("g729") G729,
    @JsonProperty("pcmu") PCMU,
    @JsonProperty("pcma") PCMA,
    @JsonProperty("speex") SPEEX,
    @JsonProperty("gsm") GSM,
    @JsonProperty("g726_32") G726_32,
    @JsonProperty("g721") G721,
    @JsonProperty("g726_24") G726_24,
    @JsonProperty("g726_40") G726_40,
    @JsonProperty("g726_16") G726_16,
    @JsonProperty("l16") L16,
    @JsonProperty("amr") AMR,
    @JsonProperty("amr_wb") AMR_WB;
}
