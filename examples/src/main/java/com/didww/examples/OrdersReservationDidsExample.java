package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.AvailableDid;
import com.didww.sdk.resource.DidReservation;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.ReservationDidOrderItem;

import java.util.Arrays;
import java.util.List;

public class OrdersReservationDidsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Get available DIDs
        QueryParams params = QueryParams.builder()
                .include("did_group.stock_keeping_units")
                .build();
        List<AvailableDid> availableDids = client.availableDids().list(params).getData();
        AvailableDid availableDid = availableDids.get(0);
        System.out.println("Available DID: " + availableDid.getNumber());

        // Reserve selected DID
        DidReservation reservation = new DidReservation();
        reservation.setAvailableDid(availableDid);
        reservation.setDescription("java sdk");

        DidReservation created = client.didReservations().create(reservation).getData();
        System.out.println("Reserved: " + created.getDescription()
                + " expires=" + created.getExpireAt()
                + " created=" + created.getCreatedAt());

        // Purchase reserved DID
        ReservationDidOrderItem orderItem = new ReservationDidOrderItem();
        orderItem.setDidReservationId(created.getId());
        orderItem.setSkuId("sku-uuid-here"); // Get from did_group.stock_keeping_units

        Order order = new Order();
        order.setItems(Arrays.asList(orderItem));

        Order orderCreated = client.orders().create(order).getData();
        System.out.println("Order " + orderCreated.getId()
                + " status=" + orderCreated.getStatus()
                + " items=" + orderCreated.getItems().size());
    }
}
