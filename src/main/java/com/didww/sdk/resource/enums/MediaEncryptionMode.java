package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MediaEncryptionMode {
    @JsonProperty("disabled") DISABLED,
    @JsonProperty("srtp_sdes") SRTP_SDES,
    @JsonProperty("srtp_dtls") SRTP_DTLS,
    @JsonProperty("zrtp") ZRTP;
}
