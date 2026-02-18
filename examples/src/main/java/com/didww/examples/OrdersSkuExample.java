package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.DidOrderItem;
import com.didww.sdk.resource.orderitem.OrderItem;

import java.util.Arrays;

public class OrdersSkuExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // To get different sku_id see DidGroupsExample
        DidOrderItem didItem = new DidOrderItem();
        didItem.setSkuId("82460535-2b3f-43a6-bcdd-62f3da0d9fa6");
        didItem.setQty(2);

        Order order = new Order();
        order.setItems(Arrays.asList(didItem));

        Order created = client.orders().create(order).getData();
        System.out.println("Order " + created.getId()
                + " amount=" + created.getAmount()
                + " status=" + created.getStatus()
                + " ref=" + created.getReference());

        OrderItem orderItem = created.getItems().get(0);
        System.out.println("Item type=" + orderItem.getType());
    }
}
