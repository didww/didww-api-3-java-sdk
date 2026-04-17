package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("address_requirement_validations")

@Getter
public class AddressRequirementValidation extends BaseResource {

    @Relationship("requirement")
    private AddressRequirement requirement;

    @Relationship("address")
    private Address address;

    @Relationship("identity")
    private Identity identity;

    public void setRequirement(AddressRequirement requirement) {
        this.requirement = markDirty("requirement", requirement);
    }

    public void setAddress(Address address) {
        this.address = markDirty("address", address);
    }

    public void setIdentity(Identity identity) {
        this.identity = markDirty("identity", identity);
    }
}
