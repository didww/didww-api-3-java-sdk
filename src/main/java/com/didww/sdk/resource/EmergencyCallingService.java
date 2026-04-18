package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.EmergencyCallingServiceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Customer-owned subscription to emergency calling on one or more DIDs.
 * Supported operations: index, show, destroy. Introduced in API 2026-04-16.
 */
@Type("emergency_calling_services")
@Getter
public class EmergencyCallingService extends BaseResource {

    /** Human-readable name for the calling service subscription. */
    @JsonProperty("name")
    private String name;

    /** Server-assigned reference code. */
    @JsonProperty("reference")
    private String reference;

    /**
     * One of: "active", "canceled", "changes_required",
     * "in_process", "new", "pending_update".
     */
    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private EmergencyCallingServiceStatus status;

    /** Timestamp when the service became active. Null while pending. */
    @JsonProperty(value = "activated_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime activatedAt;

    /** Timestamp when the service was canceled. Null when active. */
    @JsonProperty(value = "canceled_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime canceledAt;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    /** Next renewal date. Null when canceled. */
    @JsonProperty(value = "renew_date", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime renewDate;

    @Relationship("country")
    private Country country;

    @Relationship("did_group_type")
    private DidGroupType didGroupType;

    @Relationship("order")
    private Order order;

    @Relationship("emergency_requirement")
    private EmergencyRequirement emergencyRequirement;

    @Relationship("emergency_verification")
    private EmergencyVerification emergencyVerification;

    @Relationship("address")
    private Address address;

    @Relationship("dids")
    private List<Did> dids;

    @JsonIgnore
    public boolean isActive() {
        return EmergencyCallingServiceStatus.ACTIVE.equals(status);
    }

    @JsonIgnore
    public boolean isCanceled() {
        return EmergencyCallingServiceStatus.CANCELED.equals(status);
    }

    @JsonIgnore
    public boolean isChangesRequired() {
        return EmergencyCallingServiceStatus.CHANGES_REQUIRED.equals(status);
    }

    @JsonIgnore
    public boolean isInProcess() {
        return EmergencyCallingServiceStatus.IN_PROCESS.equals(status);
    }

    @JsonIgnore
    public boolean isNewStatus() {
        return EmergencyCallingServiceStatus.NEW.equals(status);
    }

    @JsonIgnore
    public boolean isPendingUpdate() {
        return EmergencyCallingServiceStatus.PENDING_UPDATE.equals(status);
    }
}
