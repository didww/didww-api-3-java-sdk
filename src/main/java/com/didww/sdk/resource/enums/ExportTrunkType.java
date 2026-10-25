package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Values of the {@code trunk_type} filter of an {@code sms_in} export. */
public enum ExportTrunkType {
    @JsonProperty("SMPP SMSC") SMPP_SMSC,
    @JsonProperty("SMPP ESME") SMPP_ESME,
    @JsonProperty("Email") EMAIL,
    @JsonProperty("HTTP IN") HTTP_IN;
}
