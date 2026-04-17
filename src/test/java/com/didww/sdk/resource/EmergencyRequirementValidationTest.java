package com.didww.sdk.resource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmergencyRequirementValidationTest {

    @Test
    void testHasEmergencyRequirementRelationship() {
        EmergencyRequirementValidation validation = new EmergencyRequirementValidation();
        EmergencyRequirement req = new EmergencyRequirement();
        validation.setEmergencyRequirement(req);
        assertThat(validation.getEmergencyRequirement()).isSameAs(req);
    }

    @Test
    void testHasAddressRelationship() {
        EmergencyRequirementValidation validation = new EmergencyRequirementValidation();
        Address address = new Address();
        validation.setAddress(address);
        assertThat(validation.getAddress()).isSameAs(address);
    }

    @Test
    void testHasIdentityRelationship() {
        EmergencyRequirementValidation validation = new EmergencyRequirementValidation();
        Identity identity = new Identity();
        validation.setIdentity(identity);
        assertThat(validation.getIdentity()).isSameAs(identity);
    }
}
