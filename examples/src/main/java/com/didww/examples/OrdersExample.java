package com.didww.examples;

import com.didww.sdk.DidwwClient;
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

        DidOrderItem didItem = new DidOrderItem();
        didItem.setSkuId("sku-uuid-here");
        didItem.setQty(1);
        didItem.setDidGroupId("did-group-uuid-here");

        newOrder.setItems(Arrays.asList(didItem));

        Order created = client.orders().create(newOrder).getData();
        System.out.println("Created order: " + created.getId() + " - " + created.getStatus());

        // Delete order (cancel)
        client.orders().delete(created.getId());
        System.out.println("Order cancelled");
    }
}
