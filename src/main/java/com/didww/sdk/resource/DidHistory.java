package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Immutable ownership-history records for DIDs in the customer's account.
 * Introduced in API 2026-04-16. Records are retained for the last 90 days only.
 *
 * Available filters (server-side):
 *   did_number (eq), action (eq), method (eq),
 *   created_at_gteq, created_at_lteq
 */
@Type("did_history")
@Getter
public class DidHistory extends BaseResource {

    @JsonProperty("did_number")
    private String didNumber;

    @JsonProperty("action")
    private String action;

    @JsonProperty("method")
    private String method;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
