package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.AreaLevel;
import com.didww.sdk.resource.enums.IdentityType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class RequirementTest extends BaseTest {

    @Test
    void testListRequirements() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/requirements"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("requirements/index.json"))));

        ApiResponse<List<Requirement>> response = client.requirements().list();
        List<Requirement> requirements = response.getData();

        assertThat(requirements).isNotEmpty();
        assertThat(requirements).hasSize(5);
        assertThat(requirements.get(0).getId()).isEqualTo("b6c80acb-3952-4d53-9e62-fe2348c0636b");
        assertThat(requirements.get(0).getIdentityType()).isEqualTo(IdentityType.ANY);
    }

    @Test
    void testFindRequirement() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/requirements/25d12afe-1ec6-4fe3-9621-b250dd1fb959"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("requirements/show.json"))));

        ApiResponse<Requirement> response = client.requirements().find("25d12afe-1ec6-4fe3-9621-b250dd1fb959");
        Requirement requirement = response.getData();

        assertThat(requirement.getId()).isEqualTo("25d12afe-1ec6-4fe3-9621-b250dd1fb959");
        assertThat(requirement.getIdentityType()).isEqualTo(IdentityType.ANY);
        assertThat(requirement.getPersonalAreaLevel()).isEqualTo(AreaLevel.WORLDWIDE);
        assertThat(requirement.getPersonalProofQty()).isEqualTo(1);
        assertThat(requirement.getServiceDescriptionRequired()).isTrue();
        assertThat(requirement.getPersonalAreaLevel()).isEqualTo(AreaLevel.WORLDWIDE);
        assertThat(requirement.getBusinessAreaLevel()).isEqualTo(AreaLevel.WORLDWIDE);
        assertThat(requirement.getAddressAreaLevel()).isEqualTo(AreaLevel.WORLDWIDE);
        assertThat(requirement.getPersonalProofQty()).isEqualTo(1);
        assertThat(requirement.getBusinessProofQty()).isEqualTo(1);
        assertThat(requirement.getAddressProofQty()).isEqualTo(1);
        assertThat(requirement.getRestrictionMessage()).isEqualTo("End User Registration is Required");
        assertThat(requirement.getPersonalMandatoryFields()).isNotNull();
        assertThat(requirement.getPersonalMandatoryFields()).contains("Birth Date");
        assertThat(requirement.getBusinessMandatoryFields()).isNotNull();
        assertThat(requirement.getBusinessMandatoryFields()).contains("Company ID");
        // relationship accessors
        assertThat(requirement.getCountry()).isNotNull();
        assertThat(requirement.getCountry().getId()).isEqualTo("5b156dc2-327e-4665-bdc5-35cd8729b885");
        assertThat(requirement.getDidGroupType()).isNotNull();
        assertThat(requirement.getDidGroupType().getId()).isEqualTo("994ea201-4a4d-4b27-ac4b-b5916ac969a3");
        assertThat(requirement.getPersonalPermanentDocument()).isNotNull();
        assertThat(requirement.getPersonalPermanentDocument().getId()).isEqualTo("fd38c86d-b69b-4ca8-b73c-286a3b93d107");
        assertThat(requirement.getBusinessPermanentDocument()).isNotNull();
        assertThat(requirement.getBusinessPermanentDocument().getId()).isEqualTo("fd38c86d-b69b-4ca8-b73c-286a3b93d107");
        assertThat(requirement.getPersonalOnetimeDocument()).isNotNull();
        assertThat(requirement.getPersonalOnetimeDocument().getId()).isEqualTo("206ccec2-1166-461f-9f58-3a56823db548");
        assertThat(requirement.getBusinessOnetimeDocument()).isNotNull();
        assertThat(requirement.getBusinessOnetimeDocument().getId()).isEqualTo("206ccec2-1166-461f-9f58-3a56823db548");
        assertThat(requirement.getPersonalProofTypes()).hasSize(1);
        assertThat(requirement.getBusinessProofTypes()).hasSize(7);
        assertThat(requirement.getAddressProofTypes()).hasSize(1);
    }
}
