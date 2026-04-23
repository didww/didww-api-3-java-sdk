package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

/**
 * Validates a prospective emergency calling service order against
 * an EmergencyRequirement, given an Address and an Identity.
 *
 * A successful POST returns 201 Created with the validation resource
 * (id mirrors the submitted emergency_requirement_id).
 * Introduced in API 2026-04-16.
 */
@Type("emergency_requirement_validations")
@Getter
public class EmergencyRequirementValidation extends BaseResource {

    @Relationship("emergency_requirement")
    private EmergencyRequirement emergencyRequirement;

    @Relationship("address")
    private Address address;

    @Relationship("identity")
    private Identity identity;

    public void setEmergencyRequirement(EmergencyRequirement emergencyRequirement) {
        this.emergencyRequirement = markDirty("emergencyRequirement", emergencyRequirement);
    }

    public void setAddress(Address address) {
        this.address = markDirty("address", address);
    }

    public void setIdentity(Identity identity) {
        this.identity = markDirty("identity", identity);
    }
}
