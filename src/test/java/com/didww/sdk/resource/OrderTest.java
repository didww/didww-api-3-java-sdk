package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.orderitem.DidOrderItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTest extends BaseTest {

    @Test
    void testFindOrder() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/orders/9df11dac-9d83-448c-8866-19c998be33db"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders/show.json"))));

        ApiResponse<Order> response = client.orders().find("9df11dac-9d83-448c-8866-19c998be33db");
        Order order = response.getData();

        assertThat(order.getId()).isEqualTo("9df11dac-9d83-448c-8866-19c998be33db");
        assertThat(order.getStatus()).isEqualTo("Completed");
        assertThat(order.getDescription()).isEqualTo("Payment processing fee");
        assertThat(order.getReference()).isEqualTo("SPT-474057");
        assertThat(order.getItems()).isNotEmpty();
    }

    @Test
    void testCreateOrder() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/orders"))
                .withRequestBody(equalToJson(loadFixture("orders/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders/create.json"))));

        DidOrderItem item1 = new DidOrderItem();
        item1.setSkuId("acc46374-0b34-4912-9f67-8340339db1e5");
        item1.setQty(2);

        DidOrderItem item2 = new DidOrderItem();
        item2.setSkuId("f36d2812-2195-4385-85e8-e59c3484a8bc");
        item2.setQty(1);

        Order order = new Order();
        order.setAllowBackOrdering(true);
        order.setItems(Arrays.asList(item1, item2));

        ApiResponse<Order> response = client.orders().create(order);
        Order created = response.getData();

        assertThat(created.getId()).isEqualTo("5da18706-be9f-49b0-aeec-0480aacd49ad");
        assertThat(created.getStatus()).isEqualTo("Pending");
        assertThat(created.getDescription()).isEqualTo("DID");
        assertThat(created.getItems()).hasSize(2);
    }
}
