package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.Pop;
import com.didww.sdk.resource.VoiceInTrunk;
import com.didww.sdk.resource.VoiceInTrunkGroup;
import com.didww.sdk.resource.configuration.SipConfiguration;
import com.didww.sdk.resource.enums.Codec;
import com.didww.sdk.resource.enums.TransportProtocol;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Demonstrates exclusive trunk/trunk group assignment on DIDs.
 * Assigning a trunk auto-nullifies the trunk group and vice versa.
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

        // Get a POP
        Pop pop = client.pops().list().getData().get(0);

        // Create a trunk
        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setName("Assignment Trunk " + suffix);
        SipConfiguration sip = new SipConfiguration();
        sip.setHost("sip.example.com");
        sip.setPort(5060);
        sip.setCodecIds(Arrays.asList(Codec.PCMU, Codec.PCMA));
        sip.setTransportProtocolId(TransportProtocol.UDP);
        trunk.setConfiguration(sip);
        trunk.setPop(pop);
        trunk = client.voiceInTrunks().create(trunk).getData();
        System.out.println("Created trunk: " + trunk.getId());

        // Create a trunk group
        VoiceInTrunkGroup group = new VoiceInTrunkGroup();
        group.setName("Assignment Group " + suffix);
        group.setCapacityLimit(10);
        group = client.voiceInTrunkGroups().create(group).getData();
        System.out.println("Created trunk group: " + group.getId());

        String trunkId = trunk.getId();
        String groupId = group.getId();

        try {
            // 1. Assign trunk to DID (auto-nullifies trunk group)
            Did update1 = Did.build(did.getId());
            update1.setVoiceInTrunk(VoiceInTrunk.build(trunkId));
            client.dids().update(update1);
            System.out.println("\n1. Assigned trunk:");
            printDidAssignment(client, did.getId());

            // 2. Assign trunk group to DID (auto-nullifies trunk)
            Did update2 = Did.build(did.getId());
            update2.setVoiceInTrunkGroup(VoiceInTrunkGroup.build(groupId));
            client.dids().update(update2);
            System.out.println("\n2. Assigned trunk group:");
            printDidAssignment(client, did.getId());

            // 3. Re-assign trunk (auto-nullifies trunk group again)
            Did update3 = Did.build(did.getId());
            update3.setVoiceInTrunk(VoiceInTrunk.build(trunkId));
            client.dids().update(update3);
            System.out.println("\n3. Re-assigned trunk:");
            printDidAssignment(client, did.getId());

            // 4. Unassign: update description only (trunk stays assigned)
            Did update4 = Did.build(did.getId());
            update4.setDescription("DID with trunk assigned");
            client.dids().update(update4);
            System.out.println("\n4. Updated description only (trunk stays):");
            printDidAssignment(client, did.getId());
        } finally {
            // Cleanup: reassign DID to group (frees trunk), delete trunk, then delete group
            try {
                Did cleanup = Did.build(did.getId());
                cleanup.setVoiceInTrunkGroup(VoiceInTrunkGroup.build(groupId));
                client.dids().update(cleanup);
            } catch (Exception ignored) {}
            try {
                client.voiceInTrunks().delete(trunkId);
                System.out.println("\nDeleted trunk");
            } catch (Exception e) {
                System.out.println("\nTrunk cleanup: " + e.getMessage());
            }
            // Reassign DID to trunk (frees group) using a temporary trunk, or just delete group
            try {
                client.voiceInTrunkGroups().delete(groupId);
                System.out.println("Deleted trunk group");
            } catch (Exception e) {
                System.out.println("Trunk group still assigned to DID (expected): " + e.getMessage());
            }
        }
    }
}
