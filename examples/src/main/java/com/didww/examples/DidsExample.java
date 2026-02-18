package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.CapacityPool;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.VoiceInTrunk;

import java.util.List;

public class DidsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Get last ordered DID
        QueryParams didParams = QueryParams.builder()
                .sort("-created_at")
                .page(1, 1)
                .build();
        Did did = client.dids().list(didParams).getData().get(0);

        // Get last SIP trunk
        QueryParams trunkParams = QueryParams.builder()
                .sort("-created_at")
                .filter("configuration.type", "sip_configurations")
                .build();
        VoiceInTrunk trunk = client.voiceInTrunks().list(trunkParams).getData().get(0);

        // Assign trunk to DID
        did.setVoiceInTrunk(trunk);
        QueryParams includeParams = QueryParams.builder()
                .include("voice_in_trunk")
                .build();
        Did updatedDid = client.dids().update(did, includeParams).getData();
        System.out.println("Assigned trunk: " + updatedDid.getVoiceInTrunk().getName());

        // Assign capacity pool and shared capacity group
        List<CapacityPool> capacityPools = client.capacityPools().list(null).getData();
        CapacityPool pool = capacityPools.stream()
                .filter(p -> "Extended".equals(p.getName()))
                .findFirst()
                .orElse(capacityPools.get(0));

        did.setCapacityPool(pool);
        did.setDedicatedChannelsCount(1);
        did.setCapacityLimit(5);
        did.setDescription("Hi");

        Did savedDid = client.dids().update(did).getData();
        System.out.println("DID " + savedDid.getId()
                + " description=" + savedDid.getDescription()
                + " capacityLimit=" + savedDid.getCapacityLimit()
                + " dedicatedChannels=" + savedDid.getDedicatedChannelsCount());
    }
}
