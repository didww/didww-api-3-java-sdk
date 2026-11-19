package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.DidGroup;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.DidOrderItem;
import com.didww.sdk.resource.orderitem.OrderItem;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Orders example: lists the most recent orders, orders a DID by SKU and cancels (deletes) that order.
 * 2026-04-16: external_reference_id is a customer-supplied tag (max 100 chars).
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.OrdersExample
 */

public class OrdersExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // List the most recent orders
        QueryParams recent = QueryParams.builder()
                .sort("-created_at")
                .page(1, 5)
                .build();
        List<Order> orders = client.orders().list(recent).getData();
        System.out.println("Recent orders (" + orders.size() + "):");
        for (Order order : orders) {
            System.out.println("Order " + order.getId() + ": " + order.getStatus()
                    + " ($" + order.getAmount() + ") created " + order.getCreatedAt());
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    System.out.println("  - " + item.getType());
                }
            }
            if (order.getExternalReferenceId() != null) {
                System.out.println("  External reference: " + order.getExternalReferenceId());
            }
        }

        // Create an order with a DID order item, by SKU
        Order newOrder = new Order();
        newOrder.setAllowBackOrdering(false);

        DidOrderItem didItem = new DidOrderItem();
        didItem.setSkuId(findSkuId(client));
        didItem.setQty(1);

        // 2026-04-16 external_reference_id — customer-supplied tag (max 100 chars)
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        newOrder.setExternalReferenceId("java-order-" + suffix);
        newOrder.setItems(Arrays.asList(didItem));

        Order created = client.orders().create(newOrder).getData();
        System.out.println("\nCreated order: " + created.getId() + " - " + created.getStatus());
        System.out.println("  External reference: " + created.getExternalReferenceId());

        // Delete order (cancel)
        client.orders().delete(created.getId());
        System.out.println("Order canceled");
    }

    // Returns the first SKU of the first DID group that has one.
    private static String findSkuId(DidwwClient client) {
        QueryParams params = QueryParams.builder()
                .include("stock_keeping_units")
                .page(1, 10)
                .build();
        for (DidGroup group : client.didGroups().list(params).getData()) {
            if (group.getStockKeepingUnits() != null && !group.getStockKeepingUnits().isEmpty()) {
                return group.getStockKeepingUnits().get(0).getId();
            }
        }
        throw new IllegalStateException("No DID group with stock_keeping_units found");
    }
}
