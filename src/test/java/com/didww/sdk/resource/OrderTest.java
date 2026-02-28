package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.CallbackMethod;
import com.didww.sdk.resource.enums.OrderStatus;
import com.didww.sdk.resource.orderitem.AvailableDidOrderItem;
import com.didww.sdk.resource.orderitem.CapacityOrderItem;
import com.didww.sdk.resource.orderitem.DidOrderItem;
import com.didww.sdk.resource.orderitem.GenericOrderItem;
import com.didww.sdk.resource.orderitem.ReservationDidOrderItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

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
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
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
        assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(created.getDescription()).isEqualTo("DID");
        assertThat(created.getItems()).hasSize(2);
    }

    @Test
    void testOrderBillingCyclesCountSave() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/orders"))
                .withRequestBody(equalToJson(loadFixture("orders/create_request_billing_cycles.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders/create_billing_cycles.json"))));

        DidOrderItem item = new DidOrderItem();
        item.setSkuId("f36d2812-2195-4385-85e8-e59c3484a8bc");
        item.setQty(1);
        item.setBillingCyclesCount(5);

        Order order = new Order();
        order.setAllowBackOrdering(true);
        order.setItems(Collections.singletonList(item));

        ApiResponse<Order> response = client.orders().create(order);
        Order created = response.getData();

        assertThat(created.getId()).isEqualTo("9b9f2121-8d9e-4aa8-9754-dbaf6f695fd6");
        assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(created.getDescription()).isEqualTo("DID");
        assertThat(created.getItems()).hasSize(1);
    }

    @Test
    void testOrderAvailableDidSave() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/orders"))
                .withRequestBody(equalToJson(loadFixture("orders/create_request_available_did.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders/create_available_did.json"))));

        AvailableDidOrderItem item = new AvailableDidOrderItem();
        item.setSkuId("acc46374-0b34-4912-9f67-8340339db1e5");
        item.setAvailableDidId("c43441e3-82d4-4d84-93e2-80998576c1ce");

        Order order = new Order();
        order.setItems(Collections.singletonList(item));

        ApiResponse<Order> response = client.orders().create(order);
        Order created = response.getData();

        assertThat(created.getId()).isEqualTo("9b9f2121-8d9e-4aa8-9754-dbaf6f695fd6");
        assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(created.getDescription()).isEqualTo("DID");
        assertThat(created.getItems()).hasSize(1);
        assertThat(created.getItems().get(0)).isInstanceOf(DidOrderItem.class);
    }

    @Test
    void testOrderReservationSave() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/orders"))
                .withRequestBody(equalToJson(loadFixture("orders/create_request_reservation.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders/create_reservation.json"))));

        ReservationDidOrderItem item = new ReservationDidOrderItem();
        item.setSkuId("32840f64-5c3f-4278-8c8d-887fbe2f03f4");
        item.setDidReservationId("e3ed9f97-1058-430c-9134-38f1c614ee9f");

        Order order = new Order();
        order.setItems(Collections.singletonList(item));

        ApiResponse<Order> response = client.orders().create(order);
        Order created = response.getData();

        assertThat(created.getId()).isEqualTo("a9a7ff2d-d634-4545-bf28-dfda92d1c723");
        assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(created.getDescription()).isEqualTo("DID");
        assertThat(created.getItems()).hasSize(1);
        assertThat(created.getItems().get(0)).isInstanceOf(DidOrderItem.class);
    }

    @Test
    void testOrderCapacitySave() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/orders"))
                .withRequestBody(equalToJson(loadFixture("orders/create_request_capacity.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders/create_capacity.json"))));

        CapacityOrderItem item = new CapacityOrderItem();
        item.setCapacityPoolId("b7522a31-4bf3-4c23-81e8-e7a14b23663f");
        item.setQty(1);

        Order order = new Order();
        order.setItems(Collections.singletonList(item));

        ApiResponse<Order> response = client.orders().create(order);
        Order created = response.getData();

        assertThat(created.getId()).isEqualTo("68a46dd5-d405-4283-b7a5-62503267e9f8");
        assertThat(created.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(created.getDescription()).isEqualTo("Capacity");
        assertThat(created.getItems()).hasSize(1);
        assertThat(created.getItems().get(0)).isInstanceOf(CapacityOrderItem.class);
    }

    @Test
    void testCreateOrderWithNanpaPrefix() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/orders"))
                .withRequestBody(equalToJson(loadFixture("orders/create_request_nanpa.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders/create_nanpa.json"))));

        DidOrderItem item = new DidOrderItem();
        item.setSkuId("fe77889c-f05a-40ad-a845-96aca3c28054");
        item.setNanpaPrefixId("eeed293b-f3d8-4ef8-91ef-1b077d174b3b");
        item.setQty(1);

        Order order = new Order();
        order.setAllowBackOrdering(true);
        order.setItems(Collections.singletonList(item));

        ApiResponse<Order> response = client.orders().create(order);
        Order created = response.getData();

        assertThat(created.getId()).isEqualTo("c617f0ff-f819-477f-a17b-a8d248c4443e");
        assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(created.getDescription()).isEqualTo("DID");
        assertThat(created.getItems()).hasSize(1);
    }

    @Test
    void testFindOrderWithGenericItem() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/orders/9df11dac-9d83-448c-8866-19c998be33db"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders/show_generic_item.json"))));

        ApiResponse<Order> response = client.orders().find("9df11dac-9d83-448c-8866-19c998be33db");
        Order order = response.getData();

        assertThat(order.getId()).isEqualTo("9df11dac-9d83-448c-8866-19c998be33db");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getDescription()).isEqualTo("Payment processing fee");
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0)).isInstanceOf(GenericOrderItem.class);
    }

    @Test
    void testOrderSkuSaveWithCallback() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/orders"))
                .withRequestBody(equalToJson(loadFixture("orders_with_callback/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("orders_with_callback/create.json"))));

        DidOrderItem item = new DidOrderItem();
        item.setSkuId("f36d2812-2195-4385-85e8-e59c3484a8bc");
        item.setQty(1);

        Order order = new Order();
        order.setAllowBackOrdering(true);
        order.setCallbackUrl("https://example.com/callback");
        order.setCallbackMethod(CallbackMethod.POST);
        order.setItems(Collections.singletonList(item));

        ApiResponse<Order> response = client.orders().create(order);
        Order created = response.getData();

        assertThat(created.getId()).isEqualTo("5da18706-be9f-49b0-aeec-0480aacd49ad");
        assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(created.getCallbackUrl()).isEqualTo("https://example.com/callback");
        assertThat(created.getCallbackMethod()).isEqualTo(CallbackMethod.POST);
        assertThat(created.getItems()).hasSize(1);
    }
}
