package com.didww.sdk;

import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.http.ApiKeyInterceptor;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptedFileUploadTest extends BaseTest {

    @Test
    void testUploadEncryptedFile() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("encrypted_files/create.json"))));

        String id = client.uploadEncryptedFile(
                "example".getBytes(StandardCharsets.UTF_8),
                "sample.pdf.enc",
                "fingerprint-123",
                "sample.pdf"
        );

        assertThat(id).isEqualTo("6eed102c-66a9-4a9b-a95f-4312d70ec12a");

        wireMock.verify(postRequestedFor(urlPathEqualTo("/v3/encrypted_files"))
                .withHeader("Api-Key", equalTo("test-api-key"))
                .withHeader(ApiKeyInterceptor.API_VERSION_HEADER, equalTo(ApiKeyInterceptor.API_VERSION))
                .withHeader("User-Agent", equalTo(SdkVersion.userAgent()))
                .withRequestBody(containing("encrypted_files[encryption_fingerprint]"))
                .withRequestBody(containing("fingerprint-123"))
                .withRequestBody(containing("encrypted_files[description]"))
                .withRequestBody(containing("sample.pdf"))
                .withRequestBody(containing("encrypted_files[file]"))
                .withRequestBody(containing("sample.pdf.enc")));
    }

    @Test
    void testUploadEncryptedFileWithoutDescription() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("encrypted_files/create.json"))));

        String id = client.uploadEncryptedFile(
                "example".getBytes(StandardCharsets.UTF_8),
                "sample.pdf.enc",
                "fingerprint-123",
                null
        );

        assertThat(id).isEqualTo("6eed102c-66a9-4a9b-a95f-4312d70ec12a");
    }

    @Test
    void testUploadEncryptedFileUnexpectedResponse() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody("{}")));

        assertThatThrownBy(() -> client.uploadEncryptedFile(
                "example".getBytes(StandardCharsets.UTF_8),
                "sample.pdf.enc",
                "fingerprint-123",
                "sample.pdf"
        )).isInstanceOf(DidwwClientException.class)
                .hasMessageContaining("Unexpected encrypted_files upload response");
    }
}
