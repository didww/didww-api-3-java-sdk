package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.CapacityPool;
import com.didww.sdk.resource.QtyBasedPricing;
import com.didww.sdk.resource.SharedCapacityGroup;

import java.util.List;

/**
 * Capacity Pools: list with included shared capacity groups and qty-based pricings.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.CapacityPoolsExample
 */
public class CapacityPoolsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // List capacity pools with includes
        QueryParams params = QueryParams.builder()
                .include("shared_capacity_groups", "qty_based_pricings")
                .build();
        List<CapacityPool> pools = client.capacityPools().list(params).getData();
        System.out.println("Capacity pools (" + pools.size() + "):");

        for (CapacityPool pool : pools) {
            System.out.println("\n  " + pool.getName());
            System.out.println("    total channels: " + pool.getTotalChannelsCount());
            System.out.println("    assigned channels: " + pool.getAssignedChannelsCount());
            System.out.println("    renew date: " + pool.getRenewDate());

            // Shared capacity groups (included)
            List<SharedCapacityGroup> groups = pool.getSharedCapacityGroups();
            if (groups != null && !groups.isEmpty()) {
                System.out.println("    shared capacity groups (" + groups.size() + "):");
                for (SharedCapacityGroup g : groups) {
                    System.out.println("      " + g.getName()
                            + " shared=" + g.getSharedChannelsCount()
                            + " metered=" + g.getMeteredChannelsCount());
                }
            }

            // Qty-based pricings (included)
            List<QtyBasedPricing> pricings = pool.getQtyBasedPricings();
            if (pricings != null && !pricings.isEmpty()) {
                System.out.println("    qty-based pricings (" + pricings.size() + "):");
                for (QtyBasedPricing p : pricings) {
                    System.out.println("      qty=" + p.getQty()
                            + " setup=" + p.getSetupPrice()
                            + " monthly=" + p.getMonthlyPrice());
                }
            }
        }
    }
}
