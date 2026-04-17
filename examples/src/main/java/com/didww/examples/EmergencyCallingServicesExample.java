package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.EmergencyCallingService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists and cancels customer Emergency Calling Services (2026-04-16).
 *
 * An EmergencyCallingService represents a customer's 911/112 subscription
 * attached to one or more DIDs. It ties an address, identity, DID group type
 * and country together.
 *
 * Supported operations: index, show, destroy (cancel).
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.EmergencyCallingServicesExample
 */
public class EmergencyCallingServicesExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        System.out.println("=== Emergency Calling Services ===");
        QueryParams params = QueryParams.builder()
                .include("country", "did_group_type", "dids")
                .build();
        List<EmergencyCallingService> services = client.emergencyCallingServices().list(params).getData();
        System.out.println("Found " + services.size() + " emergency calling services");

        int limit = Math.min(5, services.size());
        for (int i = 0; i < limit; i++) {
            EmergencyCallingService svc = services.get(i);
            System.out.println("\nService: " + svc.getId());
            System.out.println("  Name: " + svc.getName());
            System.out.println("  Reference: " + svc.getReference());
            System.out.println("  Status: " + svc.getStatus());
            if (svc.getCountry() != null) {
                System.out.println("  Country: " + svc.getCountry().getName());
            }
            if (svc.getDidGroupType() != null) {
                System.out.println("  DID Group Type: " + svc.getDidGroupType().getName());
            }
            System.out.println("  Activated: " + svc.getActivatedAt());
            if (svc.getCanceledAt() != null) {
                System.out.println("  Canceled: " + svc.getCanceledAt());
            }
            if (svc.getRenewDate() != null) {
                System.out.println("  Renews: " + svc.getRenewDate());
            }
            if (svc.getDids() != null && !svc.getDids().isEmpty()) {
                String numbers = svc.getDids().stream()
                        .map(Did::getNumber)
                        .collect(Collectors.joining(", "));
                System.out.println("  Attached DIDs: " + numbers);
            }
        }

        // Filter by status
        System.out.println("\n=== Only active emergency calling services ===");
        QueryParams activeParams = QueryParams.builder()
                .filter("status", "active")
                .build();
        List<EmergencyCallingService> active = client.emergencyCallingServices().list(activeParams).getData();
        System.out.println("Found " + active.size() + " active services");
    }
}
