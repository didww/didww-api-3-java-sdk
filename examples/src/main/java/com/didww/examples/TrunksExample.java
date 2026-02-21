package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Pop;
import com.didww.sdk.resource.VoiceInTrunk;
import com.didww.sdk.resource.configuration.SipConfiguration;
import com.didww.sdk.resource.enums.CliFormat;
import com.didww.sdk.resource.enums.Codec;
import com.didww.sdk.resource.enums.TransportProtocol;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TrunksExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // List voice in trunks with included relationships
        QueryParams params = QueryParams.builder()
                .include("pop", "voice_in_trunk_group")
                .page(1, 10)
                .build();
        List<VoiceInTrunk> trunks = client.voiceInTrunks().list(params).getData();
        for (VoiceInTrunk trunk : trunks) {
            System.out.println(trunk.getName() + " [" + trunk.getConfiguration().getType() + "]");
        }

        // Create a SIP trunk
        VoiceInTrunk newTrunk = new VoiceInTrunk();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        newTrunk.setName("My SIP Trunk " + suffix);
        newTrunk.setPriority(1);
        newTrunk.setWeight(100);
        newTrunk.setCliFormat(CliFormat.E164);
        newTrunk.setRingingTimeout(30);
        newTrunk.setCapacityLimit(10);

        SipConfiguration sip = new SipConfiguration();
        sip.setHost("sip.example.com");
        sip.setPort(5060);
        sip.setCodecIds(Arrays.asList(Codec.PCMU, Codec.PCMA));
        sip.setTransportProtocolId(TransportProtocol.UDP);
        newTrunk.setConfiguration(sip);

        // Set POP relationship
        List<Pop> pops = client.pops().list().getData();
        if (pops.isEmpty()) {
            throw new IllegalStateException("No POPs found");
        }
        Pop pop = pops.get(0);
        newTrunk.setPop(pop);

        VoiceInTrunk created = client.voiceInTrunks().create(newTrunk).getData();
        System.out.println("Created trunk: " + created.getId() + " - " + created.getName());

        // Update trunk
        created.setDescription("Updated description");
        VoiceInTrunk updated = client.voiceInTrunks().update(created).getData();
        System.out.println("Updated trunk: " + updated.getDescription());

        // Delete trunk
        client.voiceInTrunks().delete(created.getId());
        System.out.println("Trunk deleted");
    }
}
