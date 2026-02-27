package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class AvailableDidTest extends BaseTest {

    @Test
    void testListAvailableDids() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/available_dids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("available_dids/index.json"))));

        ApiResponse<List<AvailableDid>> response = client.availableDids().list();
        List<AvailableDid> availableDids = response.getData();

        assertThat(availableDids).isNotEmpty();
    }

    @Test
    void testFindAvailableDid() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/available_dids/0b76223b-9625-412f-b0f3-330551473e7e"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("available_dids/show.json"))));

        QueryParams params = QueryParams.builder()
                .include("did_group.stock_keeping_units")
                .build();
        ApiResponse<AvailableDid> response = client.availableDids().find("0b76223b-9625-412f-b0f3-330551473e7e", params);
        AvailableDid availableDid = response.getData();

        assertThat(availableDid.getNumber()).isEqualTo("16169886810");
        assertThat(availableDid.getDidGroup()).isNotNull();
        assertThat(availableDid.getDidGroup().getPrefix()).isEqualTo("616");
        assertThat(availableDid.getDidGroup().getStockKeepingUnits()).hasSize(2);
    }

    @Test
    void testFindAvailableDidWithNanpaPrefix() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/available_dids/0e1c548e-c6b5-43b0-9c12-2e300178e820"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("available_dids/show_with_nanpa_prefix.json"))));

        QueryParams params = QueryParams.builder()
                .include("nanpa_prefix")
                .build();
        ApiResponse<AvailableDid> response = client.availableDids().find("0e1c548e-c6b5-43b0-9c12-2e300178e820", params);
        AvailableDid availableDid = response.getData();

        assertThat(availableDid.getNumber()).isEqualTo("12012213879");

        NanpaPrefix nanpaPrefix = availableDid.getNanpaPrefix();
        assertThat(nanpaPrefix).isNotNull();
        assertThat(nanpaPrefix.getId()).isEqualTo("1e622e21-c740-4d3f-a615-2a7ef4991922");
        assertThat(nanpaPrefix.getNpa()).isEqualTo("201");
        assertThat(nanpaPrefix.getNxx()).isEqualTo("221");
    }
}
