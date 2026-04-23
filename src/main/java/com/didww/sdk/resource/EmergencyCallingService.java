package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.EmergencyCallingServiceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Meta;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Customer-owned subscription to emergency calling on one or more DIDs.
 * Supported operations: index, show, destroy. Introduced in API 2026-04-16.
 *
 * <p>Each resource includes server-provided {@code meta} with pricing information:
 * <ul>
 *   <li>{@code setup_price} - one-time setup fee (as a decimal string, e.g. "5.0")</li>
 *   <li>{@code monthly_price} - recurring monthly fee (as a decimal string, e.g. "3.75")</li>
 * </ul>
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

    /** Next renewal date (date-only, e.g. "2026-05-22"). Null when canceled. */
    @JsonProperty(value = "renew_date", access = JsonProperty.Access.WRITE_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate renewDate;

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

    /**
     * Server-provided meta containing pricing information.
     * Keys: "setup_price", "monthly_price".
     */
    @Meta
    private Map<String, String> meta;

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
