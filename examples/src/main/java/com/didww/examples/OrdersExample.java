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
 * Orders example — demonstrates creating and listing orders.
 * 2026-04-16: external_reference_id is a customer-supplied tag (max 100 chars).
 */

public class OrdersExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // List orders
        List<Order> orders = client.orders().list().getData();
        for (Order order : orders) {
            System.out.println("Order " + order.getId() + ": " + order.getStatus()
                    + " ($" + order.getAmount() + ")");
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    System.out.println("  - " + item.getType());
                }
            }
            if (order.getExternalReferenceId() != null) {
                System.out.println("  External reference: " + order.getExternalReferenceId());
            }
        }

        // Create an order with DID order items
        Order newOrder = new Order();
        newOrder.setAllowBackOrdering(false);

        QueryParams params = QueryParams.builder()
                .include("stock_keeping_units")
                .page(1, 1)
                .build();
        List<DidGroup> didGroups = client.didGroups().list(params).getData();
        if (didGroups.isEmpty()
                || didGroups.get(0).getStockKeepingUnits() == null
                || didGroups.get(0).getStockKeepingUnits().isEmpty()) {
            throw new IllegalStateException("No DID group with stock_keeping_units found");
        }
        String skuId = didGroups.get(0).getStockKeepingUnits().get(0).getId();

        DidOrderItem didItem = new DidOrderItem();
        didItem.setSkuId(skuId);
        didItem.setQty(1);

        // 2026-04-16 external_reference_id — customer-supplied tag (max 100 chars)
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        newOrder.setExternalReferenceId("java-order-" + suffix);
        newOrder.setItems(Arrays.asList(didItem));

        Order created = client.orders().create(newOrder).getData();
        System.out.println("Created order: " + created.getId() + " - " + created.getStatus());
        System.out.println("  External reference: " + created.getExternalReferenceId());

        // Delete order (cancel)
        client.orders().delete(created.getId());
        System.out.println("Order canceled");
    }
}
