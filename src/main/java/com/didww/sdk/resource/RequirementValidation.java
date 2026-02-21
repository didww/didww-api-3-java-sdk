package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("requirement_validations")
public class RequirementValidation extends BaseResource {

    public static RequirementValidation build(String id) {
        return BaseResource.build(RequirementValidation.class, id);
    }

    @Relationship("requirement")
    private Requirement requirement;

    @Relationship("address")
    private Address address;

    @Relationship("identity")
    private Identity identity;

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }
}
