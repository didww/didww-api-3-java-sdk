package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.AvailableDid;
import com.didww.sdk.resource.DidReservation;

import java.util.List;

/**
 * DID Reservations: create, list, find, and delete.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.DidReservationsExample
 */
public class DidReservationsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Get an available DID to reserve
        QueryParams adParams = QueryParams.builder()
                .include("did_group.stock_keeping_units")
                .page(1, 1)
                .build();
        List<AvailableDid> availableDids = client.availableDids().list(adParams).getData();
        if (availableDids.isEmpty()) {
            throw new IllegalStateException("No available DIDs found");
        }
        AvailableDid availableDid = availableDids.get(0);
        System.out.println("Reserving DID: " + availableDid.getNumber());

        // Create a reservation
        DidReservation reservation = new DidReservation();
        reservation.setDescription("SDK example reservation");
        reservation.setAvailableDid(availableDid);
        reservation = client.didReservations().create(reservation).getData();
        System.out.println("Created reservation: " + reservation.getId());
        System.out.println("  description: " + reservation.getDescription());
        System.out.println("  expires at: " + reservation.getExpireAt());

        // List reservations with includes
        QueryParams params = QueryParams.builder()
                .include("available_did")
                .build();
        List<DidReservation> reservations = client.didReservations().list(params).getData();
        System.out.println("\nAll reservations (" + reservations.size() + "):");
        for (DidReservation r : reservations) {
            String number = r.getAvailableDid() != null ? r.getAvailableDid().getNumber() : "unknown";
            System.out.println("  " + r.getId() + " - " + number);
        }

        // Find by ID
        DidReservation found = client.didReservations().find(reservation.getId()).getData();
        System.out.println("\nFound reservation: " + found.getId());

        // Delete reservation
        client.didReservations().delete(reservation.getId());
        System.out.println("Deleted reservation");
    }
}
