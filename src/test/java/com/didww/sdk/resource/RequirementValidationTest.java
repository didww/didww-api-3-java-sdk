package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequirementValidationTest extends BaseTest {

    @Test
    void testCreateRequirementValidation() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/requirement_validations"))
                .withRequestBody(equalToJson(loadFixture("requirement_validations/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("requirement_validations/create.json"))));

        RequirementValidation validation = new RequirementValidation();
        validation.setAddress(Address.build("d3414687-40f4-4346-a267-c2c65117d28c"));
        validation.setRequirement(Requirement.build("aea92b24-a044-4864-9740-89d3e15b65c7"));

        ApiResponse<RequirementValidation> response = client.requirementValidations().create(validation);
        RequirementValidation created = response.getData();

        assertThat(created.getId()).isEqualTo("aea92b24-a044-4864-9740-89d3e15b65c7");
    }

    @Test
    void testCreateRequirementValidationFailed() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/requirement_validations"))
                .withRequestBody(equalToJson(loadFixture("requirement_validations/create_request_failed.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("requirement_validations/create_1.json"))));

        RequirementValidation validation = new RequirementValidation();
        validation.setIdentity(Identity.build("5e9df058-50d2-4e34-b0d4-d1746b86f41a"));
        validation.setAddress(Address.build("d3414687-40f4-4346-a267-c2c65117d28c"));
        validation.setRequirement(Requirement.build("2efc3427-8ba6-4d50-875d-f2de4a068de8"));

        assertThatThrownBy(() -> client.requirementValidations().create(validation))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> {
                    DidwwApiException ex = (DidwwApiException) thrown;
                    assertThat(ex.getHttpStatus()).isEqualTo(422);
                    assertThat(ex.getErrors()).hasSize(3);
                    assertThat(ex.getErrors().get(0).getDetail()).isEqualTo("Identity Place of Birth must be Belgium");
                });
    }
}
