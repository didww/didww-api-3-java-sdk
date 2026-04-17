package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.EmergencyVerificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Verification record for an emergency calling service, containing the
 * address/DIDs being declared and the outcome of the compliance check.
 *
 * Supported operations: index, show, create. Introduced in API 2026-04-16.
 */
@Type("emergency_verifications")
@Getter
public class EmergencyVerification extends BaseResource {

    @JsonProperty("reference")
    private String reference;

    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private EmergencyVerificationStatus status;

    @JsonProperty(value = "reject_reasons", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> rejectReasons;

    @JsonProperty(value = "reject_comment", access = JsonProperty.Access.WRITE_ONLY)
    private String rejectComment;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("callback_method")
    private String callbackMethod;

    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @Relationship("address")
    private Address address;

    @Relationship("emergency_calling_service")
    private EmergencyCallingService emergencyCallingService;

    @Relationship("dids")
    private List<Did> dids;

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = markDirty("callbackUrl", callbackUrl);
    }

    public void setCallbackMethod(String callbackMethod) {
        this.callbackMethod = markDirty("callbackMethod", callbackMethod);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }

    public void setAddress(Address address) {
        this.address = markDirty("address", address);
    }

    public void setEmergencyCallingService(EmergencyCallingService emergencyCallingService) {
        this.emergencyCallingService = markDirty("emergencyCallingService", emergencyCallingService);
    }

    public void setDids(List<Did> dids) {
        this.dids = markDirty("dids", dids);
    }

    @JsonIgnore
    public boolean isPending() {
        return EmergencyVerificationStatus.PENDING.equals(status);
    }

    @JsonIgnore
    public boolean isApproved() {
        return EmergencyVerificationStatus.APPROVED.equals(status);
    }

    @JsonIgnore
    public boolean isRejected() {
        return EmergencyVerificationStatus.REJECTED.equals(status);
    }
}
