package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class AreaTest extends BaseTest {

    @Test
    void testListAreas() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/areas"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("areas/index.json"))));

        ApiResponse<List<Area>> response = client.areas().list();
        List<Area> areas = response.getData();

        assertThat(areas).isNotEmpty();
        assertThat(areas.get(0).getId()).isEqualTo("5df464d5-fcc4-4a82-817a-00111c990020");
        assertThat(areas.get(0).getName()).isEqualTo("Aalst");
    }

    @Test
    void testFindArea() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/areas/ab2adc18-7c94-42d9-bdde-b28dfc373a22"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("areas/show.json"))));

        QueryParams params = QueryParams.builder()
                .include("country")
                .build();
        ApiResponse<Area> response = client.areas().find("ab2adc18-7c94-42d9-bdde-b28dfc373a22", params);
        Area area = response.getData();

        assertThat(area.getId()).isEqualTo("ab2adc18-7c94-42d9-bdde-b28dfc373a22");
        assertThat(area.getName()).isEqualTo("Tuscany");
    }
}
