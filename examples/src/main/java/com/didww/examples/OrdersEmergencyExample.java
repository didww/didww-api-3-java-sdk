package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.EmergencyCallingService;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.orderitem.EmergencyOrderItem;
import com.didww.sdk.resource.orderitem.OrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Inspects Emergency orders (2026-04-16).
 *
 * Emergency orders are created server-side when an EmergencyCallingService
 * is activated or renewed - customers cannot POST them directly. They
 * appear in GET /orders alongside DID/capacity/NANPA orders.
 *
 * Each Emergency order carries items of type "emergency_order_items":
 *   - qty, emergency_calling_service_id (request)
 *   - nrc, mrc, prorated_mrc, billed_from, billed_to (response)
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.OrdersEmergencyExample
 */
public class OrdersEmergencyExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        System.out.println("=== All Orders (filtering for Emergency) ===");
        List<Order> orders = client.orders().list(null).getData();
        List<Order> emergencyOrders = new ArrayList<>();
        for (Order o : orders) {
            if ("Emergency".equals(o.getDescription())) {
                emergencyOrders.add(o);
            }
        }
        System.out.println("Found " + emergencyOrders.size() + " emergency orders out of " + orders.size() + " total");

        int limit = Math.min(5, emergencyOrders.size());
        for (int i = 0; i < limit; i++) {
            Order order = emergencyOrders.get(i);
            System.out.println("\nOrder: " + order.getId());
            System.out.println("  Reference: " + order.getReference());
            System.out.println("  Status: " + order.getStatus());
            System.out.println("  Amount: " + order.getAmount());
            System.out.println("  Created: " + order.getCreatedAt());
            if (order.getExternalReferenceId() != null) {
                System.out.println("  External Reference: " + order.getExternalReferenceId());
            }

            if (order.getItems() != null) {
                for (int j = 0; j < order.getItems().size(); j++) {
                    OrderItem item = order.getItems().get(j);
                    if (item instanceof EmergencyOrderItem) {
                        EmergencyOrderItem eItem = (EmergencyOrderItem) item;
                        System.out.println("  Item #" + (j + 1) + " (emergency_order_items):");
                        System.out.println("    Qty: " + eItem.getQty());
                        System.out.println("    Emergency Calling Service ID: " + eItem.getEmergencyCallingServiceId());
                        System.out.println("    NRC: " + eItem.getNrc());
                        System.out.println("    MRC: " + eItem.getMrc());
                        System.out.println("    Prorated MRC: " + eItem.getProratedMrc());
                        System.out.println("    Billed From: " + eItem.getBilledFrom());
                        System.out.println("    Billed To: " + eItem.getBilledTo());
                    }
                }
            }
        }

        // Follow the link from an EmergencyCallingService to its order
        System.out.println("\n=== Emergency Calling Service -> Order ===");
        QueryParams ecsParams = QueryParams.builder()
                .include("order")
                .build();
        List<EmergencyCallingService> services = client.emergencyCallingServices().list(ecsParams).getData();
        if (!services.isEmpty()) {
            EmergencyCallingService svc = services.get(0);
            System.out.println("ECS " + svc.getId() + " (" + svc.getName() + ")");
            if (svc.getOrder() != null) {
                System.out.println("  -> Order " + svc.getOrder().getId()
                        + " - status: " + svc.getOrder().getStatus()
                        + ", amount: " + svc.getOrder().getAmount());
            } else {
                System.out.println("  -> No order linked yet");
            }
        }
    }
}
