package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OnCliMismatchAction {
    @JsonProperty("send_original_cli") SEND_ORIGINAL_CLI,
    @JsonProperty("reject_call") REJECT_CALL,
    /** Requires account configuration. Contact DIDWW support to enable. */
    @JsonProperty("replace_cli") REPLACE_CLI,
    /** Requires account configuration. Contact DIDWW support to enable. */
    @JsonProperty("randomize_cli") RANDOMIZE_CLI;
}
