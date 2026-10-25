package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.SmsTrunk;
import com.didww.sdk.resource.SmsTrunkGroup;

import java.util.List;

/**
 * Routes a DID's inbound SMS (2026-10-22) to an SMS trunk, then to an SMS trunk group. The two are
 * exclusive: assigning one sends the other as null in the same PATCH, just like voice IN trunks and
 * trunk groups. At the end the DID's original SMS routing is restored, also when a step fails.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.DidSmsRoutingExample
 */
public class DidSmsRoutingExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        Did did = findDid(client);
        if (did == null) {
            return;
        }
        SmsTrunk trunk = findInboundTrunk(client);
        if (trunk == null) {
            return;
        }
        SmsTrunkGroup group = findTrunkGroup(client);

        // Remember the current routing to put it back at the end
        String originalTrunkId = did.getSmsTrunk() != null ? did.getSmsTrunk().getId() : null;
        String originalGroupId = did.getSmsTrunkGroup() != null ? did.getSmsTrunkGroup().getId() : null;
        System.out.println("\nCurrent SMS routing:");
        printSmsRouting(client, did.getId());

        try {
            // 1. Assign the SMS trunk (sends sms_trunk_group as null)
            System.out.println("\n=== 1. Assigning SMS trunk to DID ===");
            Did assignTrunk = new Did().withId(did.getId());
            assignTrunk.setSmsTrunk(new SmsTrunk().withId(trunk.getId()));
            client.dids().update(assignTrunk);
            printSmsRouting(client, did.getId());
            printRoutedDids(client, trunk);

            // 2. Assign the SMS trunk group (sends sms_trunk as null)
            if (group != null) {
                System.out.println("\n=== 2. Assigning SMS trunk group to DID (clears the SMS trunk) ===");
                Did assignGroup = new Did().withId(did.getId());
                assignGroup.setSmsTrunkGroup(new SmsTrunkGroup().withId(group.getId()));
                client.dids().update(assignGroup);
                printSmsRouting(client, did.getId());
            }
        } finally {
            // 3. Restore the original routing. Setting both to null unroutes the DID's inbound SMS.
            System.out.println("\n=== 3. Restoring the DID's original SMS routing ===");
            Did restore = new Did().withId(did.getId());
            if (originalTrunkId != null) {
                restore.setSmsTrunk(new SmsTrunk().withId(originalTrunkId));
            } else if (originalGroupId != null) {
                restore.setSmsTrunkGroup(new SmsTrunkGroup().withId(originalGroupId));
            } else {
                restore.setSmsTrunk(null);
                restore.setSmsTrunkGroup(null);
            }
            client.dids().update(restore);
            printSmsRouting(client, did.getId());
        }
    }

    private static Did findDid(DidwwClient client) {
        System.out.println("=== Finding DID ===");
        QueryParams params = QueryParams.builder()
                .include("sms_trunk", "sms_trunk_group")
                .page(1, 1)
                .build();
        List<Did> dids = client.dids().list(params).getData();
        if (dids.isEmpty()) {
            System.out.println("No DIDs found. Please order a DID first.");
            return null;
        }
        Did did = dids.get(0);
        System.out.println("Using DID: " + did.getNumber() + " (" + did.getId() + ")");
        return did;
    }

    private static SmsTrunk findInboundTrunk(DidwwClient client) {
        System.out.println("\n=== Finding inbound SMS trunk ===");
        for (SmsTrunk trunk : client.smsTrunks().list().getData()) {
            if (Boolean.TRUE.equals(trunk.getAllowIn())) {
                System.out.println("Selected SMS trunk: " + trunk.getName() + " (" + trunk.getId() + ")");
                return trunk;
            }
        }
        System.out.println("No inbound SMS trunk found. Run SmsTrunksExample or create one first.");
        return null;
    }

    // Returns the first SMS trunk group, or null when there is none.
    private static SmsTrunkGroup findTrunkGroup(DidwwClient client) {
        System.out.println("\n=== Finding SMS trunk group ===");
        List<SmsTrunkGroup> groups = client.smsTrunkGroups()
                .list(QueryParams.builder().page(1, 1).build()).getData();
        if (groups.isEmpty()) {
            System.out.println("No SMS trunk group found; step 2 is skipped.");
            return null;
        }
        SmsTrunkGroup group = groups.get(0);
        System.out.println("Selected SMS trunk group: " + group.getName() + " (" + group.getId() + ")");
        return group;
    }

    private static void printSmsRouting(DidwwClient client, String didId) {
        QueryParams params = QueryParams.builder()
                .include("sms_trunk", "sms_trunk_group")
                .build();
        Did result = client.dids().find(didId, params).getData();
        System.out.println("   sms_trunk       = " + (result.getSmsTrunk() != null ? result.getSmsTrunk().getId() : "null"));
        System.out.println("   sms_trunk_group = "
                + (result.getSmsTrunkGroup() != null ? result.getSmsTrunkGroup().getId() : "null"));
    }

    private static void printRoutedDids(DidwwClient client, SmsTrunk trunk) {
        System.out.println("\n=== DIDs routed to " + trunk.getName() + " ===");
        QueryParams params = QueryParams.builder()
                .filter("sms_trunk.id", trunk.getId())
                .build();
        for (Did d : client.dids().list(params).getData()) {
            System.out.println("   " + d.getNumber());
        }
    }
}
