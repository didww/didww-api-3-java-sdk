package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.CapacityPool;
import com.didww.sdk.resource.SharedCapacityGroup;

import java.util.UUID;

public class SharedCapacityGroupsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Get a capacity pool
        CapacityPool capacityPool = client.capacityPools().list(null).getData().get(0);

        // Create a shared capacity group
        SharedCapacityGroup group = new SharedCapacityGroup();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        group.setName("My Channel Group " + suffix);
        group.setMeteredChannelsCount(10);
        group.setSharedChannelsCount(1);
        group.setCapacityPool(capacityPool);

        SharedCapacityGroup created = client.sharedCapacityGroups().create(group).getData();
        System.out.println("Created: " + created.getId()
                + " name=" + created.getName()
                + " metered=" + created.getMeteredChannelsCount()
                + " shared=" + created.getSharedChannelsCount());
    }
}
