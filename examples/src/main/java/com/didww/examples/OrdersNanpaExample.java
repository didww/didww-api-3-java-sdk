package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.DidGroup;
import com.didww.sdk.resource.NanpaPrefix;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.DidOrderItem;

import java.util.Collections;
import java.util.List;

public class OrdersNanpaExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Step 1: find the NANPA prefix by NPA/NXX (e.g. 201-221)
        QueryParams nanpaParams = QueryParams.builder()
                .filter("npanxx", "201221")
                .page(1, 1)
                .build();
        List<NanpaPrefix> nanpaPrefixes = client.nanpaPrefixes().list(nanpaParams).getData();
        if (nanpaPrefixes.isEmpty()) {
            throw new IllegalStateException("NANPA prefix 201-221 not found");
        }
        NanpaPrefix nanpaPrefix = nanpaPrefixes.get(0);
        System.out.println("NANPA prefix: " + nanpaPrefix.getId()
                + " NPA=" + nanpaPrefix.getNpa() + " NXX=" + nanpaPrefix.getNxx());

        // Step 2: find a DID group for this prefix and load its SKUs
        QueryParams dgParams = QueryParams.builder()
                .filter("nanpa_prefix.id", nanpaPrefix.getId())
                .include("stock_keeping_units")
                .page(1, 1)
                .build();
        List<DidGroup> didGroups = client.didGroups().list(dgParams).getData();
        if (didGroups.isEmpty()
                || didGroups.get(0).getStockKeepingUnits() == null
                || didGroups.get(0).getStockKeepingUnits().isEmpty()) {
            throw new IllegalStateException("No DID group with SKUs found for this NANPA prefix");
        }
        DidGroup didGroup = didGroups.get(0);
        String skuId = didGroup.getStockKeepingUnits().get(0).getId();
        System.out.println("DID group: " + didGroup.getId() + "  SKU: " + skuId);

        // Step 3: create the order
        DidOrderItem item = new DidOrderItem();
        item.setSkuId(skuId);
        item.setNanpaPrefixId(nanpaPrefix.getId());
        item.setQty(1);

        Order order = new Order();
        order.setAllowBackOrdering(true);
        order.setItems(Collections.singletonList(item));

        Order created = client.orders().create(order).getData();
        System.out.println("Order " + created.getId()
                + " amount=" + created.getAmount()
                + " status=" + created.getStatus()
                + " ref=" + created.getReference());
    }
}
