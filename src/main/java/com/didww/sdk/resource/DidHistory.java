package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Meta;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Immutable ownership-history records for DIDs in the customer's account.
 * Introduced in API 2026-04-16. Records are retained for the last 90 days only.
 *
 * Available filters (server-side):
 *   did_number (eq), action (eq), method (eq),
 *   created_at_gteq, created_at_lteq
 *
 * Meta fields (present only when action == "billing_cycles_count_changed"):
 *   from (String) - the previous billing_cycles_count value
 *   to   (String) - the new billing_cycles_count value
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

    @Meta
    private Map<String, String> meta;

    /**
     * Returns the previous billing_cycles_count value from the resource meta.
     * Only present when action is "billing_cycles_count_changed".
     *
     * @return the "from" meta value, or null if not present
     */
    @JsonIgnore
    public String getMetaFrom() {
        return meta != null ? meta.get("from") : null;
    }

    /**
     * Returns the new billing_cycles_count value from the resource meta.
     * Only present when action is "billing_cycles_count_changed".
     *
     * @return the "to" meta value, or null if not present
     */
    @JsonIgnore
    public String getMetaTo() {
        return meta != null ? meta.get("to") : null;
    }
}
