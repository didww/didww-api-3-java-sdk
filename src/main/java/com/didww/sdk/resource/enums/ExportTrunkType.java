package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Values of the {@code trunk_type} filter of an {@code sms_in} export. */
public enum ExportTrunkType {
    @JsonProperty("smpp_smsc") SMPP_SMSC,
    @JsonProperty("smpp_esme") SMPP_ESME,
    @JsonProperty("sms_to_email") SMS_TO_EMAIL,
    @JsonProperty("http_in") HTTP_IN;
}
