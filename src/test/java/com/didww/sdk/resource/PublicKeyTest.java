package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class PublicKeyTest extends BaseTest {

    @Test
    void testListPublicKeys() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/public_keys"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("public_keys/index.json"))));

        ApiResponse<List<PublicKey>> response = client.publicKeys().list();
        List<PublicKey> keys = response.getData();

        assertThat(keys).isNotEmpty();
        assertThat(keys).hasSize(2);
        assertThat(keys.get(0).getId()).isEqualTo("dcf2bfcb-a1d0-3b58-bbf0-3ec22a510ba8");
        assertThat(keys.get(0).getKey()).startsWith("-----BEGIN PUBLIC KEY-----");

        wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/public_keys"))
                .withoutHeader("Api-Key"));
    }
}
