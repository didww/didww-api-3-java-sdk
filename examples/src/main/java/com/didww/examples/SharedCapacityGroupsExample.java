package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.CapacityPool;
import com.didww.sdk.resource.SharedCapacityGroup;

import java.util.List;
import java.util.UUID;

/**
 * Shared Capacity Groups: creates a group in a capacity pool, lists the groups, renames the new one and
 * deletes it, also when a step fails.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.SharedCapacityGroupsExample
 */
public class SharedCapacityGroupsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Get a capacity pool
        List<CapacityPool> pools = client.capacityPools().list(QueryParams.builder().page(1, 1).build()).getData();
        if (pools.isEmpty()) {
            throw new IllegalStateException("No capacity pools found");
        }
        CapacityPool capacityPool = pools.get(0);
        System.out.println("Using capacity pool: " + capacityPool.getName() + " (" + capacityPool.getId() + ")");

        // Create a shared capacity group
        SharedCapacityGroup group = new SharedCapacityGroup();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        group.setName("My Channel Group " + suffix);
        group.setMeteredChannelsCount(10);
        group.setSharedChannelsCount(1);
        group.setCapacityPool(capacityPool);
        group.setExternalReferenceId("java-scg-" + suffix);

        SharedCapacityGroup created = client.sharedCapacityGroups().create(group).getData();
        System.out.println("Created: " + created.getId()
                + " name=" + created.getName()
                + " metered=" + created.getMeteredChannelsCount()
                + " shared=" + created.getSharedChannelsCount()
                + " ext_ref=" + created.getExternalReferenceId());

        try {
            // List shared capacity groups with their capacity pool
            QueryParams params = QueryParams.builder()
                    .include("capacity_pool")
                    .build();
            List<SharedCapacityGroup> groups = client.sharedCapacityGroups().list(params).getData();
            System.out.println("\nShared capacity groups (" + groups.size() + "):");
            for (SharedCapacityGroup g : groups) {
                String poolName = g.getCapacityPool() != null ? g.getCapacityPool().getName() : "unknown";
                System.out.println("  " + g.getName() + " shared=" + g.getSharedChannelsCount()
                        + " metered=" + g.getMeteredChannelsCount() + " pool=" + poolName);
            }

            // Rename the group: only the changed name is sent
            SharedCapacityGroup update = new SharedCapacityGroup().withId(created.getId());
            update.setName("Updated Channel Group " + suffix);
            SharedCapacityGroup updated = client.sharedCapacityGroups().update(update).getData();
            System.out.println("\nUpdated name: " + updated.getName());
        } finally {
            client.sharedCapacityGroups().delete(created.getId());
            System.out.println("Deleted shared capacity group");
        }
    }
}
