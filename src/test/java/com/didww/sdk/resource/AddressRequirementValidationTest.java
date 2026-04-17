package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressRequirementValidationTest extends BaseTest {

    @Test
    void testCreateAddressRequirementValidation() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/address_requirement_validations"))
                .withRequestBody(equalToJson(loadFixture("address_requirement_validations/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("address_requirement_validations/create.json"))));

        AddressRequirementValidation validation = new AddressRequirementValidation();
        validation.setAddress(new Address().withId("d3414687-40f4-4346-a267-c2c65117d28c"));
        validation.setRequirement(new Requirement().withId("aea92b24-a044-4864-9740-89d3e15b65c7"));

        ApiResponse<AddressRequirementValidation> response = client.addressRequirementValidations().create(validation);
        AddressRequirementValidation created = response.getData();

        assertThat(created.getId()).isEqualTo("aea92b24-a044-4864-9740-89d3e15b65c7");
    }

    @Test
    void testCreateAddressRequirementValidationFailed() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/address_requirement_validations"))
                .withRequestBody(equalToJson(loadFixture("address_requirement_validations/create_request_failed.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("address_requirement_validations/create_1.json"))));

        AddressRequirementValidation validation = new AddressRequirementValidation();
        validation.setIdentity(new Identity().withId("5e9df058-50d2-4e34-b0d4-d1746b86f41a"));
        validation.setAddress(new Address().withId("d3414687-40f4-4346-a267-c2c65117d28c"));
        validation.setRequirement(new Requirement().withId("2efc3427-8ba6-4d50-875d-f2de4a068de8"));

        assertThatThrownBy(() -> client.addressRequirementValidations().create(validation))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> {
                    DidwwApiException ex = (DidwwApiException) thrown;
                    assertThat(ex.getHttpStatus()).isEqualTo(422);
                    assertThat(ex.getErrors()).hasSize(3);
                    assertThat(ex.getErrors().get(0).getDetail()).isEqualTo("Identity Place of Birth must be Belgium");
                });
    }
}
