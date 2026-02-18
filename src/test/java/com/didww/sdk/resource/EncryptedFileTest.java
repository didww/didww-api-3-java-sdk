package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class EncryptedFileTest extends BaseTest {

    @Test
    void testListEncryptedFiles() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/encrypted_files"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("encrypted_files/index.json"))));

        ApiResponse<List<EncryptedFile>> response = client.encryptedFiles().list();
        List<EncryptedFile> files = response.getData();

        assertThat(files).isNotEmpty();
        assertThat(files.get(0).getId()).isEqualTo("7f2fbdca-8008-44ce-bcb6-3537ea5efaac");
        assertThat(files.get(0).getDescription()).isEqualTo("file.enc");
    }

    @Test
    void testFindEncryptedFile() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/encrypted_files/6eed102c-66a9-4a9b-a95f-4312d70ec12a"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("encrypted_files/show.json"))));

        ApiResponse<EncryptedFile> response = client.encryptedFiles().find("6eed102c-66a9-4a9b-a95f-4312d70ec12a");
        EncryptedFile file = response.getData();

        assertThat(file.getId()).isEqualTo("6eed102c-66a9-4a9b-a95f-4312d70ec12a");
        assertThat(file.getDescription()).isEqualTo("some description");
    }
}
