package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Group of SMS trunks (API 2026-10-22). {@code http_out} trunks cannot join a group. Deleting a group also
 * deletes its trunks.
 */
@Type("sms_trunk_groups")

@Getter
public class SmsTrunkGroup extends MetaResource {

    @JsonProperty("name")
    private String name;

    /** Customer's own identifier for the group, up to 100 characters. */
    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty(value = "updated_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime updatedAt;

    @Relationship("sms_trunks")
    private List<SmsTrunk> smsTrunks;

    /** DIDs whose inbound messages the group handles. Read-only. */
    @Relationship(value = "incoming_dids", serialise = false)
    private List<Did> incomingDids;

    /**
     * Sets the name of the group. Required on create.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = markDirty("name", name);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }

    /**
     * Sets the trunks of the group. The list replaces the stored membership; an empty list removes every trunk.
     *
     * @param smsTrunks the trunks
     */
    public void setSmsTrunks(List<SmsTrunk> smsTrunks) {
        this.smsTrunks = markDirty("smsTrunks", smsTrunks);
    }

    /**
     * Returns the number of trunks in the group from the resource meta.
     *
     * @return the "sms_trunks_count" meta value, or null if not present or not a number
     */
    @JsonIgnore
    public Integer getMetaSmsTrunksCount() {
        String count = meta != null ? meta.get("sms_trunks_count") : null;
        if (count == null) {
            return null;
        }
        try {
            return Integer.valueOf(count);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
