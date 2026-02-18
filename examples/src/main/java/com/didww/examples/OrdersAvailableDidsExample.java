package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.AvailableDid;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.AvailableDidOrderItem;

import java.util.Arrays;
import java.util.List;

public class OrdersAvailableDidsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Get available DIDs with included DID group and SKUs
        QueryParams params = QueryParams.builder()
                .include("did_group.stock_keeping_units")
                .build();
        List<AvailableDid> availableDids = client.availableDids().list(params).getData();
        AvailableDid availableDid = availableDids.get(0);
        System.out.println("Available DID: " + availableDid.getNumber());

        // Create order with available DID
        AvailableDidOrderItem orderItem = new AvailableDidOrderItem();
        orderItem.setAvailableDidId(availableDid.getId());
        orderItem.setSkuId("sku-uuid-here"); // Get from did_group.stock_keeping_units

        Order order = new Order();
        order.setItems(Arrays.asList(orderItem));

        Order created = client.orders().create(order).getData();
        System.out.println("Order " + created.getId()
                + " status=" + created.getStatus()
                + " items=" + created.getItems().size());
    }
}
