package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.AvailableDid;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.DidGroup;
import com.didww.sdk.resource.DidReservation;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.AvailableDidOrderItem;
import com.didww.sdk.resource.orderitem.DidOrderItem;
import com.didww.sdk.resource.orderitem.OrderItem;
import com.didww.sdk.resource.orderitem.ReservationDidOrderItem;

import java.util.Arrays;
import java.util.List;

/**
 * Creates a DID order with all three item types:
 * 1. DidOrderItem - order by SKU and quantity (random DID from a DID group)
 * 2. AvailableDidOrderItem - order a specific available DID
 * 3. ReservationDidOrderItem - order a previously reserved DID
 *
 * Then fetches the ordered DIDs by order ID.
 */
public class OrdersAllItemTypesExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // --- Find available DIDs with their DID group and SKUs ---
        QueryParams adParams = QueryParams.builder()
                .include("did_group.stock_keeping_units")
                .build();
        List<AvailableDid> availableDids = client.availableDids().list(adParams).getData();
        if (availableDids.size() < 2) {
            throw new IllegalStateException("Need at least 2 available DIDs for this example");
        }

        AvailableDid ad1 = availableDids.get(0);
        AvailableDid ad2 = availableDids.get(1);
        String skuId1 = getSku(ad1);
        String skuId2 = getSku(ad2);

        System.out.println("Available DID 1: " + ad1.getId() + " (" + ad1.getNumber()
                + ") sku=" + skuId1 + " - for AvailableDidOrderItem");
        System.out.println("Available DID 2: " + ad2.getId() + " (" + ad2.getNumber()
                + ") sku=" + skuId2 + " - for reservation");

        // --- Get a separate SKU for the DidOrderItem (by quantity) ---
        // Use a different DID group to avoid conflicts with ad1's pool
        QueryParams dgParams = QueryParams.builder()
                .include("stock_keeping_units")
                .page(1, 10)
                .build();
        List<DidGroup> didGroups = client.didGroups().list(dgParams).getData();
        String skuForQty = null;
        for (DidGroup dg : didGroups) {
            if (dg.getStockKeepingUnits() != null && !dg.getStockKeepingUnits().isEmpty()) {
                skuForQty = dg.getStockKeepingUnits().get(0).getId();
                System.out.println("SKU for qty order: " + skuForQty + " (group=" + dg.getId() + ")");
                break;
            }
        }
        if (skuForQty == null) {
            throw new IllegalStateException("No DID group with stock_keeping_units found");
        }

        // --- Reserve the second available DID ---
        DidReservation reservation = new DidReservation();
        reservation.setDescription("Reserved for order example");
        reservation.setAvailableDid(ad2);

        DidReservation createdReservation = client.didReservations().create(reservation).getData();
        System.out.println("Reservation: " + createdReservation.getId());

        // --- Build order with all three item types ---

        // 1. DidOrderItem - order by SKU and quantity (random DID)
        DidOrderItem itemBySku = new DidOrderItem();
        itemBySku.setSkuId(skuForQty);
        itemBySku.setQty(1);

        // 2. AvailableDidOrderItem - order a specific available DID
        AvailableDidOrderItem itemByAvailable = new AvailableDidOrderItem();
        itemByAvailable.setSkuId(skuId1);
        itemByAvailable.setAvailableDidId(ad1.getId());

        // 3. ReservationDidOrderItem - order a previously reserved DID
        ReservationDidOrderItem itemByReservation = new ReservationDidOrderItem();
        itemByReservation.setSkuId(skuId2);
        itemByReservation.setDidReservationId(createdReservation.getId());

        Order order = new Order();
        order.setItems(Arrays.asList(itemBySku, itemByAvailable, itemByReservation));

        Order created = client.orders().create(order).getData();
        System.out.println("\nOrder ID: " + created.getId());
        System.out.println("Amount: " + created.getAmount());
        System.out.println("Status: " + created.getStatus());
        System.out.println("Created at: " + created.getCreatedAt());
        System.out.println("Reference: " + created.getReference());
        System.out.println("Items count: " + created.getItems().size());
        for (int i = 0; i < created.getItems().size(); i++) {
            OrderItem item = created.getItems().get(i);
            System.out.println("  Item " + (i + 1) + ": type=" + item.getType());
        }

        // --- Fetch DIDs that belong to this order ---
        QueryParams didParams = QueryParams.builder()
                .filter("order.id", created.getId())
                .build();
        List<Did> dids = client.dids().list(didParams).getData();
        System.out.println("\nDIDs in order (" + dids.size() + "):");
        for (Did did : dids) {
            System.out.println("  " + did.getId() + " | " + did.getNumber()
                    + " | capacity_limit=" + did.getCapacityLimit());
        }
    }

    private static String getSku(AvailableDid ad) {
        if (ad.getDidGroup() == null || ad.getDidGroup().getStockKeepingUnits() == null
                || ad.getDidGroup().getStockKeepingUnits().isEmpty()) {
            throw new IllegalStateException("No stock_keeping_units for available DID " + ad.getId());
        }
        return ad.getDidGroup().getStockKeepingUnits().get(0).getId();
    }
}
