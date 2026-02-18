package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.DidGroup;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.DidOrderItem;
import com.didww.sdk.resource.orderitem.OrderItem;

import java.util.Arrays;
import java.util.List;

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

        newOrder.setItems(Arrays.asList(didItem));

        Order created = client.orders().create(newOrder).getData();
        System.out.println("Created order: " + created.getId() + " - " + created.getStatus());

        // Delete order (cancel)
        client.orders().delete(created.getId());
        System.out.println("Order cancelled");
    }
}
