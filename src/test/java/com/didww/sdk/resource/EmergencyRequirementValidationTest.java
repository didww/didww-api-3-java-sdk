package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class EmergencyRequirementValidationTest extends BaseTest {

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

    @Test
    void testCreateEmergencyRequirementValidation() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/emergency_requirement_validations"))
                .withRequestBody(equalToJson(loadFixture("emergency_requirement_validations/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("emergency_requirement_validations/create.json"))));

        EmergencyRequirementValidation validation = new EmergencyRequirementValidation();
        validation.setAddress(new Address().withId("66666666-7777-8888-9999-aaaaaaaaaaaa"));
        validation.setIdentity(new Identity().withId("bbbbbbbb-cccc-dddd-eeee-ffffffffffff"));
        validation.setEmergencyRequirement(new EmergencyRequirement().withId("11111111-2222-3333-4444-555555555555"));

        ApiResponse<EmergencyRequirementValidation> response = client.emergencyRequirementValidations().create(validation);
        EmergencyRequirementValidation created = response.getData();

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo("c1d2e3f4-a5b6-7890-1234-567890abcdef");
    }
}
