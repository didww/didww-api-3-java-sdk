package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class DidGroupTypeTest extends BaseTest {

    @Test
    void testListDidGroupTypes() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/did_group_types"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_group_types/index.json"))));

        ApiResponse<List<DidGroupType>> response = client.didGroupTypes().list();
        List<DidGroupType> didGroupTypes = response.getData();

        assertThat(didGroupTypes).isNotEmpty();
    }

    @Test
    void testFindDidGroupType() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/did_group_types/d6530a8c-924c-469a-98c0-9525602e6192"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_group_types/show.json"))));

        ApiResponse<DidGroupType> response = client.didGroupTypes().find("d6530a8c-924c-469a-98c0-9525602e6192");
        DidGroupType didGroupType = response.getData();

        assertThat(didGroupType.getName()).isEqualTo("Global");
    }
}
