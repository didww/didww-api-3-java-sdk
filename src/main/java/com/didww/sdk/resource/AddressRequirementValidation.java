package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("address_requirement_validations")

@Getter
public class AddressRequirementValidation extends BaseResource {

    @Relationship("address_requirement")
    private AddressRequirement addressRequirement;

    @Relationship("address")
    private Address address;

    @Relationship("identity")
    private Identity identity;

    public void setAddressRequirement(AddressRequirement addressRequirement) {
        this.addressRequirement = markDirty("addressRequirement", addressRequirement);
    }

    public void setAddress(Address address) {
        this.address = markDirty("address", address);
    }

    public void setIdentity(Identity identity) {
        this.identity = markDirty("identity", identity);
    }
}
