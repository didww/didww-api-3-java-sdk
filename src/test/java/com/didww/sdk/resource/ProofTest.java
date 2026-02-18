package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ProofTest extends BaseTest {

    @Test
    void testCreateProof() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/proofs"))
                .withRequestBody(equalToJson(loadFixture("proofs/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("proofs/create.json"))));

        ProofType proofType = new ProofType();
        proofType.setId("19cd7b22-559b-41d4-99c9-7ad7ad63d5d1");

        EncryptedFile encryptedFile = new EncryptedFile();
        encryptedFile.setId("254b3c2d-c40c-4ff7-93b1-a677aee7fa10");

        Proof proof = new Proof();
        proof.setProofType(proofType);
        proof.setFiles(Collections.singletonList(encryptedFile));

        ApiResponse<Proof> response = client.proofs().create(proof);
        Proof created = response.getData();

        assertThat(created.getId()).isEqualTo("ed46925b-a830-482d-917d-015858cf7ab9");
        assertThat(created.getProofType()).isNotNull();
        assertThat(created.getProofType().getId()).isEqualTo("19cd7b22-559b-41d4-99c9-7ad7ad63d5d1");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void testDeleteProof() {
        String id = "ed46925b-a830-482d-917d-015858cf7ab9";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/proofs/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.proofs().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/proofs/" + id)));
    }
}
