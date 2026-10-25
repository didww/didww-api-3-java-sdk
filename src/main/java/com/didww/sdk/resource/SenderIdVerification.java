package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.SenderIdVerificationStatus;
import com.didww.sdk.resource.enums.SenderType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Verification of a sender identity used for outbound messages (API 2026-10-22). Supported operations: index,
 * show and update of {@code name} and {@code sms_trunk}; the API has no create or delete. A closed verification
 * cannot be updated.
 *
 * <p>Server-provided {@code meta} carries {@code setup_price} and {@code monthly_price}, decimal strings.
 */
@Type("sender_id_verifications")

@Getter
public class SenderIdVerification extends MetaResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private SenderIdVerificationStatus status;

    @JsonProperty(value = "sender_type", access = JsonProperty.Access.WRITE_ONLY)
    private SenderType senderType;

    /** Sender IDs the verification covers. For long codes and toll-free numbers they are the numbers of the DIDs. */
    @JsonProperty(value = "sender_ids", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> senderIds;

    @JsonProperty(value = "reference", access = JsonProperty.Access.WRITE_ONLY)
    private String reference;

    /** Reasons the last submission was rejected. Null unless the status is changes_required. */
    @JsonProperty(value = "reject_reasons", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> rejectReasons;

    /** Reviewer comment on the rejection. Null unless the status is changes_required. */
    @JsonProperty(value = "reject_comment", access = JsonProperty.Access.WRITE_ONLY)
    private String rejectComment;

    /** Next renewal date, {@code "YYYY-MM-DD"}. Null once closed. */
    @JsonProperty(value = "renew_date", access = JsonProperty.Access.WRITE_ONLY)
    private String renewDate;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty(value = "activated_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime activatedAt;

    @JsonProperty(value = "closed_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime closedAt;

    @Relationship("sms_trunk")
    private SmsTrunk smsTrunk;

    @Relationship(value = "countries", serialise = false)
    private List<Country> countries;

    @Relationship(value = "dids", serialise = false)
    private List<Did> dids;

    @Relationship(value = "order", serialise = false)
    private Order order;

    @Relationship(value = "identity", serialise = false)
    private Identity identity;

    @Relationship(value = "address", serialise = false)
    private Address address;

    public void setName(String name) {
        this.name = markDirty("name", name);
    }

    /**
     * Sets the SMS trunk the verification is used on. Only {@code http_out}, {@code smpp_smsc} and
     * {@code smpp_esme} trunks are accepted; null on update unassigns it.
     *
     * @param smsTrunk the trunk, or null
     */
    public void setSmsTrunk(SmsTrunk smsTrunk) {
        this.smsTrunk = markDirty("smsTrunk", smsTrunk);
    }

    /**
     * Returns the one-time setup price from the resource meta.
     *
     * @return the "setup_price" meta value, or null if not present
     */
    @JsonIgnore
    public String getMetaSetupPrice() {
        return meta != null ? meta.get("setup_price") : null;
    }

    /**
     * Returns the recurring monthly price from the resource meta.
     *
     * @return the "monthly_price" meta value, or null if not present
     */
    @JsonIgnore
    public String getMetaMonthlyPrice() {
        return meta != null ? meta.get("monthly_price") : null;
    }

    @JsonIgnore
    public boolean isActive() {
        return SenderIdVerificationStatus.ACTIVE.equals(status);
    }

    @JsonIgnore
    public boolean isChangesRequired() {
        return SenderIdVerificationStatus.CHANGES_REQUIRED.equals(status);
    }

    @JsonIgnore
    public boolean isClosed() {
        return SenderIdVerificationStatus.CLOSED.equals(status);
    }

    @JsonIgnore
    public boolean isClosingInProcess() {
        return SenderIdVerificationStatus.CLOSING_IN_PROCESS.equals(status);
    }

    @JsonIgnore
    public boolean isInProcess() {
        return SenderIdVerificationStatus.IN_PROCESS.equals(status);
    }

    @JsonIgnore
    public boolean isNewStatus() {
        return SenderIdVerificationStatus.NEW.equals(status);
    }
}
