package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class NanpaPrefixTest extends BaseTest {

    @Test
    void testListNanpaPrefixes() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/nanpa_prefixes"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("nanpa_prefixes/index.json"))));

        ApiResponse<List<NanpaPrefix>> response = client.nanpaPrefixes().list();
        List<NanpaPrefix> prefixes = response.getData();

        assertThat(prefixes).isNotEmpty();
        assertThat(prefixes).hasSize(2);
        assertThat(prefixes.get(0).getId()).isEqualTo("54943e12-88e9-4df9-be54-a72926c251dd");
        assertThat(prefixes.get(0).getNpa()).isEqualTo("864");
        assertThat(prefixes.get(0).getNxx()).isEqualTo("200");
    }

    @Test
    void testFindNanpaPrefix() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/nanpa_prefixes/6c16d51d-d376-4395-91c4-012321317e48"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("nanpa_prefixes/show.json"))));

        QueryParams params = QueryParams.builder()
                .include("country")
                .build();
        ApiResponse<NanpaPrefix> response = client.nanpaPrefixes().find("6c16d51d-d376-4395-91c4-012321317e48", params);
        NanpaPrefix prefix = response.getData();

        assertThat(prefix.getId()).isEqualTo("6c16d51d-d376-4395-91c4-012321317e48");
        assertThat(prefix.getNpa()).isEqualTo("864");
        assertThat(prefix.getNxx()).isEqualTo("920");
    }
}
