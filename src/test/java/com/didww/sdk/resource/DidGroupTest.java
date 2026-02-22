package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.Feature;
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
        assertThat(didGroups.get(0).getFeatures()).containsExactly(Feature.VOICE_IN, Feature.SMS_IN, Feature.T38);
    }

    @Test
    void testFindDidGroup() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/did_groups/2187c36d-28fb-436f-8861-5a0f5b5a3ee1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_groups/show.json"))));

        QueryParams params = QueryParams.builder()
                .include("country", "region", "city", "did_group_type", "stock_keeping_units")
                .build();
        ApiResponse<DidGroup> response = client.didGroups().find("2187c36d-28fb-436f-8861-5a0f5b5a3ee1", params);
        DidGroup didGroup = response.getData();

        assertThat(didGroup.getPrefix()).isEqualTo("241");
        assertThat(didGroup.getFeatures()).containsExactly(Feature.VOICE);
        assertThat(didGroup.getAreaName()).isEqualTo("Aachen");
        assertThat(didGroup.getIsMetered()).isEqualTo(false);
        assertThat(didGroup.getCountry()).isNotNull();
        assertThat(didGroup.getCountry().getName()).isEqualTo("Germany");
        assertThat(didGroup.getCity()).isNotNull();
        assertThat(didGroup.getCity().getName()).isEqualTo("Aachen");
        assertThat(didGroup.getDidGroupType()).isNotNull();
        assertThat(didGroup.getDidGroupType().getName()).isEqualTo("Local");
        assertThat(didGroup.getRegion()).isNull();
        assertThat(didGroup.getStockKeepingUnits()).hasSize(2);
    }
}
