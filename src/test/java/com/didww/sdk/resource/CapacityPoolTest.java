package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class CapacityPoolTest extends BaseTest {

    @Test
    void testListCapacityPools() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/capacity_pools"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("capacity_pools/index.json"))));

        ApiResponse<List<CapacityPool>> response = client.capacityPools().list();
        List<CapacityPool> pools = response.getData();

        assertThat(pools).isNotEmpty();

        CapacityPool first = pools.get(0);
        assertThat(first.getId()).isEqualTo("f288d07c-e2fc-4ae6-9837-b18fb469c324");
        assertThat(first.getName()).isEqualTo("Standard");
        assertThat(first.getTotalChannelsCount()).isEqualTo(34);
        assertThat(first.getAssignedChannelsCount()).isEqualTo(24);
    }

    @Test
    void testFindCapacityPool() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/capacity_pools/f288d07c-e2fc-4ae6-9837-b18fb469c324"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("capacity_pools/show.json"))));

        QueryParams params = QueryParams.builder()
                .include("countries", "shared_capacity_groups", "qty_based_pricings")
                .build();
        ApiResponse<CapacityPool> response = client.capacityPools().find("f288d07c-e2fc-4ae6-9837-b18fb469c324", params);
        CapacityPool pool = response.getData();

        assertThat(pool.getName()).isEqualTo("Standard");
        assertThat(pool.getRenewDate()).isEqualTo("2019-01-21");
        assertThat(pool.getCountries()).hasSizeGreaterThanOrEqualTo(43);
        assertThat(pool.getSharedCapacityGroups()).hasSize(3);
        assertThat(pool.getQtyBasedPricings()).hasSize(3);
    }

    @Test
    void testUpdateCapacityPool() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/capacity_pools/f288d07c-e2fc-4ae6-9837-b18fb469c324"))
                .withRequestBody(equalToJson(loadFixture("capacity_pools/update_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("capacity_pools/update.json"))));

        CapacityPool pool = CapacityPool.build("f288d07c-e2fc-4ae6-9837-b18fb469c324");
        pool.setTotalChannelsCount(25);

        ApiResponse<CapacityPool> response = client.capacityPools().update(pool);
        CapacityPool updated = response.getData();

        assertThat(updated.getId()).isEqualTo("f288d07c-e2fc-4ae6-9837-b18fb469c324");
        assertThat(updated.getName()).isEqualTo("Standard");
        assertThat(updated.getTotalChannelsCount()).isEqualTo(25);
        assertThat(updated.getAssignedChannelsCount()).isEqualTo(24);
    }
}
