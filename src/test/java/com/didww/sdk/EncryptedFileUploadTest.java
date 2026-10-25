package com.didww.sdk;

import com.didww.sdk.exception.DidwwClientException;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.OkHttpClient;
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
                .withHeader("Authorization", equalTo("Bearer " + TEST_ACCESS_TOKEN))
                .withHeader("Accept", equalTo("application/vnd.api+json"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withHeader("User-Agent", equalTo(SdkVersion.userAgent()))
                .withoutHeader("Api-Key")
                .withoutHeader("X-DIDWW-API-Version")
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

    @Test
    void testUploadGoesThroughTheConfiguredHttpClient() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .withHeader("X-Custom-Header", equalTo("custom-value"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("encrypted_files/create.json"))));
        DidwwClient custom = clientBuilder()
                .httpClientBuilder(new OkHttpClient.Builder()
                        .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                                .header("X-Custom-Header", "custom-value")
                                .build())))
                .build();

        String id = custom.uploadEncryptedFile(
                "example".getBytes(StandardCharsets.UTF_8), "sample.pdf.enc", "fingerprint-123", null);

        assertThat(id).isEqualTo("6eed102c-66a9-4a9b-a95f-4312d70ec12a");
    }

    @Test
    void testUploadSentAgainWithNewTokenAfter401() {
        wireMock.stubFor(post(urlEqualTo("/token"))
                .inScenario("tokens").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(okJson(tokenJson("token-1", API_AUDIENCE)))
                .willSetStateTo("second"));
        wireMock.stubFor(post(urlEqualTo("/token"))
                .inScenario("tokens").whenScenarioStateIs("second")
                .willReturn(okJson(tokenJson("token-2", API_AUDIENCE))));
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .withHeader("Authorization", equalTo("Bearer token-1"))
                .willReturn(aResponse().withStatus(401)));
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .withHeader("Authorization", equalTo("Bearer token-2"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("encrypted_files/create.json"))));

        String id = client.uploadEncryptedFile(
                "example".getBytes(StandardCharsets.UTF_8), "sample.pdf.enc", "fingerprint-123", "sample.pdf");

        assertThat(id).isEqualTo("6eed102c-66a9-4a9b-a95f-4312d70ec12a");
        assertThat(mints()).isEqualTo(2);
        wireMock.verify(2, postRequestedFor(urlPathEqualTo("/v3/encrypted_files"))
                .withRequestBody(containing("fingerprint-123"))
                .withRequestBody(containing("example")));
    }

    @Test
    void testUploadErrorBodyIsLimited() {
        StringBuilder big = new StringBuilder("{\"errors\":[{\"detail\":\"too large\"}],\"pad\":\"");
        for (int i = 0; i < (2 << 20); i++) {
            big.append('x');
        }
        big.append("\"}");
        wireMock.stubFor(post(urlPathEqualTo("/v3/encrypted_files"))
                .willReturn(aResponse().withStatus(422).withBody(big.toString())));

        assertThatThrownBy(() -> client.uploadEncryptedFile(
                "example".getBytes(StandardCharsets.UTF_8), "sample.pdf.enc", "fingerprint-123", null))
                .isInstanceOf(DidwwClientException.class)
                .hasMessageStartingWith("Failed to upload encrypted file: HTTP 422 {\"errors\"")
                .satisfies(e -> assertThat(e.getMessage().length()).isLessThanOrEqualTo((1 << 20) + 100));
    }
}
