package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class SharedCapacityGroupTest extends BaseTest {

    @Test
    void testListSharedCapacityGroups() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/shared_capacity_groups"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("shared_capacity_groups/index.json"))));

        ApiResponse<List<SharedCapacityGroup>> response = client.sharedCapacityGroups().list();
        List<SharedCapacityGroup> groups = response.getData();

        assertThat(groups).isNotEmpty();

        SharedCapacityGroup first = groups.get(0);
        assertThat(first.getId()).isEqualTo("89f987e2-0862-4bf4-a3f4-cdc89af0d875");
        assertThat(first.getName()).isEqualTo("didww");
        assertThat(first.getSharedChannelsCount()).isEqualTo(19);
    }

    @Test
    void testCreateSharedCapacityGroup() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/shared_capacity_groups"))
                .withRequestBody(equalToJson(loadFixture("shared_capacity_groups/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("shared_capacity_groups/create_6.json"))));

        CapacityPool pool = new CapacityPool();
        pool.setId("f288d07c-e2fc-4ae6-9837-b18fb469c324");

        SharedCapacityGroup group = new SharedCapacityGroup();
        group.setName("php-sdk");
        group.setSharedChannelsCount(5);
        group.setMeteredChannelsCount(0);
        group.setCapacityPool(pool);

        ApiResponse<SharedCapacityGroup> response = client.sharedCapacityGroups().create(group);
        SharedCapacityGroup created = response.getData();

        assertThat(created.getId()).isEqualTo("3688a9c3-354f-4e16-b458-1d2df9f02547");
        assertThat(created.getName()).isEqualTo("php-sdk");
        assertThat(created.getSharedChannelsCount()).isEqualTo(5);
    }
}
