package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExportType {
    @JsonProperty("cdr_in") CDR_IN,
    @JsonProperty("cdr_out") CDR_OUT;
}
