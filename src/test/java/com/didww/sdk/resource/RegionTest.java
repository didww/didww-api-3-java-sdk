package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class RegionTest extends BaseTest {

    @Test
    void testListRegions() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/regions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("regions/index.json"))));

        ApiResponse<List<Region>> response = client.regions().list();
        List<Region> regions = response.getData();

        assertThat(regions).isNotEmpty();
    }

    @Test
    void testFindRegion() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/regions/c11b1f34-16cf-4ba6-8497-f305b53d5b01"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("regions/show.json"))));

        QueryParams params = QueryParams.builder()
                .include("country")
                .build();
        ApiResponse<Region> response = client.regions().find("c11b1f34-16cf-4ba6-8497-f305b53d5b01", params);
        Region region = response.getData();

        assertThat(region.getName()).isEqualTo("California");
        assertThat(region.getCountry()).isNotNull();
        assertThat(region.getCountry().getName()).isEqualTo("United States");
        assertThat(region.getCountry().getIso()).isEqualTo("US");
    }
}
