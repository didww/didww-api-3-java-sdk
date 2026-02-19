package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OnCliMismatchAction {
    @JsonProperty("send_original_cli") SEND_ORIGINAL_CLI,
    @JsonProperty("reject_call") REJECT_CALL,
    @JsonProperty("replace_cli") REPLACE_CLI;
}
