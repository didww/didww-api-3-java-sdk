package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.resource.*;
import com.didww.sdk.repository.ApiResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class PopTest extends BaseTest {

    @Test
    void testListPops() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/pops"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("pops/index.json"))));

        ApiResponse<List<Pop>> response = client.pops().list();
        List<Pop> pops = response.getData();

        assertThat(pops).isNotEmpty();

        Pop first = pops.get(0);
        assertThat(first.getId()).isEqualTo("29dbdddf-3026-4e82-a2d6-5d8b3b2e0ad9");
        assertThat(first.getName()).isEqualTo("New York, NY, USA");
    }
}
