package com.didww.sdk.resource;

import com.didww.sdk.converter.SmsTrunkConfigurationDeserializer;
import com.didww.sdk.converter.SmsTrunkConfigurationSerializer;
import com.didww.sdk.resource.smstrunkconfiguration.SmsTrunkConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * SMS trunk: how messages of the assigned DIDs are delivered, or accepted for sending (API 2026-10-22).
 * The delivery is described by the {@code configuration}; on update its type must match
 * the stored one and the attributes left out keep their values.
 *
 * <p>Changes made inside a loaded configuration are not tracked: call {@code setConfiguration(configuration)}
 * again after editing it.
 *
 * <p>Filters: {@code id}, {@code name}, {@code external_reference_id}, {@code configuration.type}.
 * Includes: {@code outgoing_dids}, {@code incoming_dids}, {@code sms_trunk_group},
 * {@code sender_id_verifications}.
 */
@Type("sms_trunks")

@Getter
public class SmsTrunk extends MetaResource {

    @JsonProperty("name")
    private String name;

    /** Customer's own identifier for the trunk, up to 100 characters. */
    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    /** True stops the trunk from handling messages. */
    @JsonProperty("blocked")
    private Boolean blocked;

    @JsonProperty("priority")
    private Integer priority;

    /**
     * Allows any DID of the customer as the message source. Turning it on empties {@code outgoing_dids}, which
     * must stay empty while it is true. Refused on the trunk types that only receive messages.
     */
    @JsonProperty("allow_any_did_as_source")
    private Boolean allowAnyDidAsSource;

    /** True when the trunk handles inbound messages. */
    @JsonProperty(value = "allow_in", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean allowIn;

    /** True when the trunk can send messages. */
    @JsonProperty(value = "allow_out", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean allowOut;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty(value = "updated_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime updatedAt;

    @JsonProperty("configuration")
    @JsonDeserialize(using = SmsTrunkConfigurationDeserializer.class)
    @JsonSerialize(using = SmsTrunkConfigurationSerializer.class)
    private SmsTrunkConfiguration configuration;

    /** DIDs accepted as the source address of a message sent through the trunk. */
    @Relationship("outgoing_dids")
    private List<Did> outgoingDids;

    /** DIDs whose inbound messages the trunk handles. Read-only. */
    @Relationship(value = "incoming_dids", serialise = false)
    private List<Did> incomingDids;

    @Relationship("sms_trunk_group")
    private SmsTrunkGroup smsTrunkGroup;

    /** Sender ID verifications assigned to the trunk. Read-only. */
    @Relationship(value = "sender_id_verifications", serialise = false)
    private List<SenderIdVerification> senderIdVerifications;

    /**
     * Sets the name of the trunk. Required on create.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = markDirty("name", name);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = markDirty("blocked", blocked);
    }

    /**
     * Sets the routing priority within an SMS trunk group, 0 to 32767; the higher value is tried first. Defaults
     * to 100 on the trunk types that can join a group and must stay null on {@code http_out}.
     *
     * @param priority the priority, or null
     */
    public void setPriority(Integer priority) {
        this.priority = markDirty("priority", priority);
    }

    /**
     * Sets whether any DID of the customer is accepted as the message source. {@code true} also clears an
     * {@code outgoing_dids} list that was loaded or set before, so a trunk read with {@code outgoing_dids} included
     * does not send its DIDs back.
     *
     * @param allowAnyDidAsSource the value
     */
    public void setAllowAnyDidAsSource(Boolean allowAnyDidAsSource) {
        this.allowAnyDidAsSource = markDirty("allowAnyDidAsSource", allowAnyDidAsSource);
        if (Boolean.TRUE.equals(allowAnyDidAsSource) && this.outgoingDids != null) {
            this.outgoingDids = markDirty("outgoingDids", Collections.emptyList());
        }
    }

    public void setConfiguration(SmsTrunkConfiguration configuration) {
        this.configuration = markDirty("configuration", configuration);
    }

    /**
     * Sets the DIDs accepted as the message source. The list replaces the stored one; an empty list (or null
     * on update) clears it.
     *
     * @param outgoingDids the DIDs
     */
    public void setOutgoingDids(List<Did> outgoingDids) {
        this.outgoingDids = markDirty("outgoingDids", outgoingDids);
    }

    /**
     * Sets the SMS trunk group the trunk belongs to; null on update leaves the group.
     *
     * @param smsTrunkGroup the group, or null
     */
    public void setSmsTrunkGroup(SmsTrunkGroup smsTrunkGroup) {
        this.smsTrunkGroup = markDirty("smsTrunkGroup", smsTrunkGroup);
    }

    /**
     * Returns the URL outbound messages are submitted to. Only {@code http_out} trunks carry it.
     *
     * @return the "host" meta value, or null if not present
     */
    @JsonIgnore
    public String getMetaHost() {
        return meta != null ? meta.get("host") : null;
    }
}
