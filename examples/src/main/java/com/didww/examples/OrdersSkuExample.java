package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.DidGroup;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.DidOrderItem;
import com.didww.sdk.resource.orderitem.OrderItem;

import java.util.Arrays;
import java.util.List;

public class OrdersSkuExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

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

        DidOrderItem didItem = new DidOrderItem();
        didItem.setSkuId(didGroups.get(0).getStockKeepingUnits().get(0).getId());
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
