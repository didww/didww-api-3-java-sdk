package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
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
        assertThat(requirement.getPersonalAreaLevel()).isEqualTo("WorldWide");
        assertThat(requirement.getPersonalProofQty()).isEqualTo(1);
        assertThat(requirement.getServiceDescriptionRequired()).isTrue();
    }
}
