package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Customer-owned subscription to emergency calling on one or more DIDs.
 * Supported operations: index, show, destroy. Introduced in API 2026-04-16.
 */
@Type("emergency_calling_services")
@Getter
public class EmergencyCallingService extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private String status;

    @JsonProperty(value = "activated_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime activatedAt;

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

    @Relationship("dids")
    private List<Did> dids;
}
