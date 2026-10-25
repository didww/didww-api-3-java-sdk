package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.VoiceInTrunk;
import com.didww.sdk.resource.VoiceInTrunkGroup;
import com.didww.sdk.resource.configuration.SipConfiguration;
import com.didww.sdk.resource.enums.Codec;
import com.didww.sdk.resource.enums.Pop;
import com.didww.sdk.resource.enums.TransportProtocol;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Demonstrates exclusive trunk/trunk group assignment on DIDs.
 * Assigning a trunk auto-nullifies the trunk group and vice versa.
 * At the end the DID's original voice routing and description are restored and the
 * created trunk and trunk group are deleted, also when a step fails.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.DidTrunkAssignmentExample
 */
public class DidTrunkAssignmentExample {

    private static void printDidAssignment(DidwwClient client, String didId) {
        QueryParams params = QueryParams.builder()
                .include("voice_in_trunk", "voice_in_trunk_group")
                .build();
        Did result = client.dids().find(didId, params).getData();
        System.out.println("   trunk = " + (result.getVoiceInTrunk() != null ? result.getVoiceInTrunk().getId() : "null"));
        System.out.println("   group = " + (result.getVoiceInTrunkGroup() != null ? result.getVoiceInTrunkGroup().getId() : "null"));
    }

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        // Get a DID to work with
        QueryParams didParams = QueryParams.builder()
                .include("voice_in_trunk", "voice_in_trunk_group")
                .page(1, 1)
                .build();
        List<Did> dids = client.dids().list(didParams).getData();
        if (dids.isEmpty()) {
            throw new IllegalStateException("No DIDs found. Order a DID first.");
        }
        Did did = dids.get(0);
        System.out.println("Using DID: " + did.getNumber() + " (" + did.getId() + ")");

        // Remember the current routing and description to put them back at the end
        String originalTrunkId = did.getVoiceInTrunk() != null ? did.getVoiceInTrunk().getId() : null;
        String originalGroupId = did.getVoiceInTrunkGroup() != null ? did.getVoiceInTrunkGroup().getId() : null;
        String originalDescription = did.getDescription();

        String trunkId = null;
        String groupId = null;
        boolean didChanged = false;
        try {
            // Create a trunk
            VoiceInTrunk trunk = new VoiceInTrunk();
            trunk.setName("Assignment Trunk " + suffix);
            SipConfiguration sip = new SipConfiguration();
            sip.setHost("sip.example.com");
            sip.setPort(5060);
            sip.setCodecIds(Arrays.asList(Codec.PCMU, Codec.PCMA));
            sip.setTransportProtocolId(TransportProtocol.UDP);
            trunk.setConfiguration(sip);
            trunk.setPop(Pop.FRA); // optional; leave it unset to let DIDWW choose
            trunkId = client.voiceInTrunks().create(trunk).getData().getId();
            System.out.println("Created trunk: " + trunkId);

            // Create a trunk group
            VoiceInTrunkGroup group = new VoiceInTrunkGroup();
            group.setName("Assignment Group " + suffix);
            group.setCapacityLimit(10);
            groupId = client.voiceInTrunkGroups().create(group).getData().getId();
            System.out.println("Created trunk group: " + groupId);

            // 1. Assign trunk to DID (auto-nullifies trunk group)
            didChanged = true;
            Did update1 = new Did().withId(did.getId());
            update1.setVoiceInTrunk(new VoiceInTrunk().withId(trunkId));
            client.dids().update(update1);
            System.out.println("\n1. Assigned trunk:");
            printDidAssignment(client, did.getId());

            // 2. Assign trunk group to DID (auto-nullifies trunk)
            Did update2 = new Did().withId(did.getId());
            update2.setVoiceInTrunkGroup(new VoiceInTrunkGroup().withId(groupId));
            client.dids().update(update2);
            System.out.println("\n2. Assigned trunk group:");
            printDidAssignment(client, did.getId());

            // 3. Re-assign trunk (auto-nullifies trunk group again)
            Did update3 = new Did().withId(did.getId());
            update3.setVoiceInTrunk(new VoiceInTrunk().withId(trunkId));
            client.dids().update(update3);
            System.out.println("\n3. Re-assigned trunk:");
            printDidAssignment(client, did.getId());

            // 4. Update the description only: only changed fields are sent, so the trunk stays
            Did update4 = new Did().withId(did.getId());
            update4.setDescription("DID with trunk assigned");
            client.dids().update(update4);
            System.out.println("\n4. Updated description only (trunk stays):");
            printDidAssignment(client, did.getId());
        } finally {
            try {
                // Restore the DID first, so the created trunk and group are no longer in use
                if (didChanged) {
                    restoreDid(client, did.getId(), originalTrunkId, originalGroupId, originalDescription);
                }
            } finally {
                try {
                    if (trunkId != null) {
                        client.voiceInTrunks().delete(trunkId);
                        System.out.println("Deleted trunk");
                    }
                } finally {
                    if (groupId != null) {
                        client.voiceInTrunkGroups().delete(groupId);
                        System.out.println("Deleted trunk group");
                    }
                }
            }
        }
    }

    private static void restoreDid(DidwwClient client, String didId, String trunkId, String groupId,
                                   String description) {
        Did restore = new Did().withId(didId);
        if (trunkId != null) {
            restore.setVoiceInTrunk(new VoiceInTrunk().withId(trunkId));
        } else if (groupId != null) {
            restore.setVoiceInTrunkGroup(new VoiceInTrunkGroup().withId(groupId));
        } else {
            restore.setVoiceInTrunk(null); // sends both trunk and trunk group as null
        }
        restore.setDescription(description);
        client.dids().update(restore);
        System.out.println("\nRestored the DID's original routing:");
        printDidAssignment(client, didId);
    }
}
