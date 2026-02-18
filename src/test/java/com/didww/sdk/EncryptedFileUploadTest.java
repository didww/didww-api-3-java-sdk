package com.didww.sdk;

import com.didww.sdk.exception.DidwwClientException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptedFileUploadTest extends BaseTest {

    @Test
    void testUploadEncryptedFile() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("encrypted_files/create.json"))));

        List<String> ids = client.uploadEncryptedFile(
                "example".getBytes(StandardCharsets.UTF_8),
                "sample.pdf.enc",
                "fingerprint-123",
                "sample.pdf"
        );

        assertThat(ids)
                .containsExactly("6eed102c-66a9-4a9b-a95f-4312d70ec12a", "371eafbd-ac6a-485c-aadf-9e3c5da37eb4");

        wireMock.verify(postRequestedFor(urlPathEqualTo("/v3/encrypted_files"))
                .withHeader("Api-Key", equalTo("test-api-key"))
                .withRequestBody(containing("encrypted_files[encryption_fingerprint]"))
                .withRequestBody(containing("fingerprint-123"))
                .withRequestBody(containing("encrypted_files[items][][description]"))
                .withRequestBody(containing("sample.pdf"))
                .withRequestBody(containing("encrypted_files[items][][file]"))
                .withRequestBody(containing("sample.pdf.enc")));
    }

    @Test
    void testUploadEncryptedFileUnexpectedResponse() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("encrypted_files/show.json"))));

        assertThatThrownBy(() -> client.uploadEncryptedFile(
                "example".getBytes(StandardCharsets.UTF_8),
                "sample.pdf.enc",
                "fingerprint-123",
                "sample.pdf"
        )).isInstanceOf(DidwwClientException.class)
                .hasMessageContaining("Unexpected encrypted_files upload response");
    }
}
