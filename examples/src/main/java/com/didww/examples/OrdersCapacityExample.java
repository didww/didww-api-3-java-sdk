package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.CapacityPool;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.CapacityOrderItem;

import java.util.Arrays;
import java.util.List;

public class OrdersCapacityExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Get capacity pools
        List<CapacityPool> capacityPools = client.capacityPools().list(null).getData();
        CapacityPool capacityPool = capacityPools.get(0);
        System.out.println("Capacity pool: " + capacityPool.getName());

        // Purchase capacity
        CapacityOrderItem orderItem = new CapacityOrderItem();
        orderItem.setCapacityPoolId(capacityPool.getId());
        orderItem.setQty(1);

        Order order = new Order();
        order.setItems(Arrays.asList(orderItem));

        Order created = client.orders().create(order).getData();
        System.out.println("Order " + created.getId()
                + " status=" + created.getStatus()
                + " items=" + created.getItems().size());
    }
}
