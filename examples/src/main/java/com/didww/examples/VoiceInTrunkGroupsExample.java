package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
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
 * Voice In Trunk Groups: CRUD operations with trunk relationships.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.VoiceInTrunkGroupsExample
 */
public class VoiceInTrunkGroupsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        // Point of presence of the trunks; leave it unset to let DIDWW choose
        Pop pop = Pop.FRA;

        // Create two trunks
        VoiceInTrunk trunkA = client.voiceInTrunks().create(sipTrunk("Group Trunk A " + suffix, "sip-a.example.com",
                pop, Arrays.asList(Codec.PCMU, Codec.PCMA))).getData();
        System.out.println("Created trunk A: " + trunkA.getId());

        VoiceInTrunk trunkB;
        try {
            trunkB = client.voiceInTrunks().create(sipTrunk("Group Trunk B " + suffix, "sip-b.example.com",
                    pop, Arrays.asList(Codec.PCMU))).getData();
        } catch (RuntimeException e) {
            client.voiceInTrunks().delete(trunkA.getId());
            throw e;
        }
        System.out.println("Created trunk B: " + trunkB.getId());

        // Create a trunk group with both trunks (2026-04-16: external_reference_id)
        VoiceInTrunkGroup group = new VoiceInTrunkGroup();
        group.setName("My Trunk Group " + suffix);
        group.setCapacityLimit(10);
        group.setExternalReferenceId("java-tg-" + suffix);
        group.setVoiceInTrunks(Arrays.asList(trunkA, trunkB));
        try {
            group = client.voiceInTrunkGroups().create(group).getData();
        } catch (RuntimeException e) {
            client.voiceInTrunks().delete(trunkA.getId());
            client.voiceInTrunks().delete(trunkB.getId());
            throw e;
        }
        System.out.println("Created trunk group: " + group.getId() + " - " + group.getName());
        System.out.println("  External Reference: " + group.getExternalReferenceId());

        try {
            // List trunk groups with included trunks
            QueryParams params = QueryParams.builder()
                    .include("voice_in_trunks")
                    .build();
            List<VoiceInTrunkGroup> groups = client.voiceInTrunkGroups().list(params).getData();
            System.out.println("\nAll trunk groups (" + groups.size() + "):");
            for (VoiceInTrunkGroup g : groups) {
                int trunkCount = g.getVoiceInTrunks() != null ? g.getVoiceInTrunks().size() : 0;
                System.out.println("  " + g.getName() + " (" + trunkCount + " trunks)"
                        + " ext_ref=" + g.getExternalReferenceId());
            }

            // Update group name
            group.setName("Updated Group " + suffix);
            VoiceInTrunkGroup updated = client.voiceInTrunkGroups().update(group).getData();
            System.out.println("\nUpdated name: " + updated.getName());
        } finally {
            // Cleanup: deleting the group cascades to delete assigned trunks
            client.voiceInTrunkGroups().delete(group.getId());
            System.out.println("Deleted trunk group (cascaded to trunks)");
        }
    }

    private static VoiceInTrunk sipTrunk(String name, String host, Pop pop, List<Codec> codecs) {
        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setName(name);
        SipConfiguration sip = new SipConfiguration();
        sip.setHost(host);
        sip.setPort(5060);
        sip.setCodecIds(codecs);
        sip.setTransportProtocolId(TransportProtocol.UDP);
        trunk.setConfiguration(sip);
        trunk.setPop(pop);
        return trunk;
    }
}
