package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.resource.*;
import com.didww.sdk.repository.ApiResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class DidGroupTest extends BaseTest {

    @Test
    void testListDidGroups() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/did_groups"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_groups/index.json"))));

        ApiResponse<List<DidGroup>> response = client.didGroups().list();
        List<DidGroup> didGroups = response.getData();

        assertThat(didGroups).isNotEmpty();
    }

    @Test
    void testFindDidGroup() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/did_groups/2187c36d-28fb-436f-8861-5a0f5b5a3ee1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_groups/show.json"))));

        ApiResponse<DidGroup> response = client.didGroups().find("2187c36d-28fb-436f-8861-5a0f5b5a3ee1");
        DidGroup didGroup = response.getData();

        assertThat(didGroup.getPrefix()).isEqualTo("241");
        assertThat(didGroup.getAreaName()).isEqualTo("Aachen");
        assertThat(didGroup.getIsMetered()).isEqualTo(false);
    }
}
