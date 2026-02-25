package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
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
 * Voice In Trunk Groups: CRUD operations with trunk relationships.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.VoiceInTrunkGroupsExample
 */
public class VoiceInTrunkGroupsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        // Get a POP for trunk creation
        List<Pop> pops = client.pops().list().getData();
        if (pops.isEmpty()) {
            throw new IllegalStateException("No POPs found");
        }
        Pop pop = pops.get(0);

        // Create two trunks
        VoiceInTrunk trunkA = new VoiceInTrunk();
        trunkA.setName("Group Trunk A " + suffix);
        SipConfiguration sipA = new SipConfiguration();
        sipA.setHost("sip-a.example.com");
        sipA.setPort(5060);
        sipA.setCodecIds(Arrays.asList(Codec.PCMU, Codec.PCMA));
        sipA.setTransportProtocolId(TransportProtocol.UDP);
        trunkA.setConfiguration(sipA);
        trunkA.setPop(pop);
        trunkA = client.voiceInTrunks().create(trunkA).getData();
        System.out.println("Created trunk A: " + trunkA.getId());

        VoiceInTrunk trunkB = new VoiceInTrunk();
        trunkB.setName("Group Trunk B " + suffix);
        SipConfiguration sipB = new SipConfiguration();
        sipB.setHost("sip-b.example.com");
        sipB.setPort(5060);
        sipB.setCodecIds(Arrays.asList(Codec.PCMU));
        sipB.setTransportProtocolId(TransportProtocol.UDP);
        trunkB.setConfiguration(sipB);
        trunkB.setPop(pop);
        trunkB = client.voiceInTrunks().create(trunkB).getData();
        System.out.println("Created trunk B: " + trunkB.getId());

        // Create a trunk group with both trunks
        VoiceInTrunkGroup group = new VoiceInTrunkGroup();
        group.setName("My Trunk Group " + suffix);
        group.setCapacityLimit(10);
        group.setVoiceInTrunks(Arrays.asList(
                VoiceInTrunk.build(trunkA.getId()),
                VoiceInTrunk.build(trunkB.getId())
        ));
        group = client.voiceInTrunkGroups().create(group).getData();
        System.out.println("Created trunk group: " + group.getId() + " - " + group.getName());

        // List trunk groups with included trunks
        QueryParams params = QueryParams.builder()
                .include("voice_in_trunks")
                .build();
        List<VoiceInTrunkGroup> groups = client.voiceInTrunkGroups().list(params).getData();
        System.out.println("\nAll trunk groups (" + groups.size() + "):");
        for (VoiceInTrunkGroup g : groups) {
            int trunkCount = g.getVoiceInTrunks() != null ? g.getVoiceInTrunks().size() : 0;
            System.out.println("  " + g.getName() + " (" + trunkCount + " trunks)");
        }

        // Update group name
        group.setName("Updated Group " + suffix);
        VoiceInTrunkGroup updated = client.voiceInTrunkGroups().update(group).getData();
        System.out.println("\nUpdated name: " + updated.getName());

        // Cleanup: deleting the group cascades to delete assigned trunks
        client.voiceInTrunkGroups().delete(group.getId());
        System.out.println("Deleted trunk group (cascaded to trunks)");
    }
}
