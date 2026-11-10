package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Values of the {@code service_type} filter of an {@code sms_out} export. (API 2026-10-22) */
public enum ExportServiceType {
    @JsonProperty("a2p") A2P,
    @JsonProperty("p2p") P2P;
}
