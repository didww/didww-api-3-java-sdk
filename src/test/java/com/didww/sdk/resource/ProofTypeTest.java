package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ProofTypeTest extends BaseTest {

    @Test
    void testListProofTypes() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/proof_types"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("proof_types/index.json"))));

        ApiResponse<List<ProofType>> response = client.proofTypes().list();
        List<ProofType> proofTypes = response.getData();

        assertThat(proofTypes).isNotEmpty();
        assertThat(proofTypes.get(0).getId()).isEqualTo("ab1fb565-ac55-4c73-bc55-64dc61e70169");
        assertThat(proofTypes.get(0).getName()).isEqualTo("Utility Bill");
        assertThat(proofTypes.get(0).getEntityType()).isEqualTo("Address");
    }
}
