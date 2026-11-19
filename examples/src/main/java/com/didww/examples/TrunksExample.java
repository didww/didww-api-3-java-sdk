package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.VoiceInTrunk;
import com.didww.sdk.resource.configuration.PstnConfiguration;
import com.didww.sdk.resource.configuration.SipConfiguration;
import com.didww.sdk.resource.enums.CliFormat;
import com.didww.sdk.resource.enums.Codec;
import com.didww.sdk.resource.enums.IceMode;
import com.didww.sdk.resource.enums.Pop;
import com.didww.sdk.resource.enums.ReroutingDisconnectCode;
import com.didww.sdk.resource.enums.TransportProtocol;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Voice IN trunks: lists trunks with their trunk group, creates a SIP trunk and a PSTN trunk, finds the
 * SIP trunk by ID, updates both trunks and deletes both, also when a step fails.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.TrunksExample
 */
public class TrunksExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        // List voice in trunks with included relationships
        QueryParams params = QueryParams.builder()
                .include("voice_in_trunk_group")
                .page(1, 10)
                .build();
        List<VoiceInTrunk> trunks = client.voiceInTrunks().list(params).getData();
        System.out.println("=== Voice IN trunks (" + trunks.size() + ") ===");
        for (VoiceInTrunk trunk : trunks) {
            printTrunk(trunk);
        }

        String sipId = null;
        String pstnId = null;
        try {
            // --- Create a SIP trunk ---
            VoiceInTrunk newTrunk = new VoiceInTrunk();
            newTrunk.setName("My SIP Trunk " + suffix);
            newTrunk.setPriority(1);
            newTrunk.setWeight(100);
            newTrunk.setCliFormat(CliFormat.E164);
            newTrunk.setRingingTimeout(30);
            newTrunk.setCapacityLimit(10);

            SipConfiguration sip = new SipConfiguration();
            sip.setHost("sip.example.com");
            sip.setPort(5060);
            // Codecs by name, in priority order; left out, DIDWW applies its default list
            sip.setCodecs(Arrays.asList(Codec.PCMU, Codec.PCMA, Codec.TELEPHONE_EVENT));
            sip.setIceMode(IceMode.ACCEPT);
            sip.setTransportProtocolId(TransportProtocol.UDP);
            sip.setReroutingDisconnectCodeIds(Arrays.asList(
                    ReroutingDisconnectCode.SIP_480_TEMPORARILY_UNAVAILABLE,
                    ReroutingDisconnectCode.SIP_503_SERVICE_UNAVAILABLE
            ));
            newTrunk.setConfiguration(sip);

            // Point of presence: a Pop constant, or leave it unset to let DIDWW choose
            newTrunk.setPop(Pop.FRA);

            VoiceInTrunk created = client.voiceInTrunks().create(newTrunk).getData();
            sipId = created.getId();
            System.out.println("\nCreated SIP trunk: " + created.getId() + " - " + created.getName()
                    + " (pop " + created.getPop() + ")");

            // --- Create a PSTN trunk ---
            VoiceInTrunk pstnTrunk = new VoiceInTrunk();
            pstnTrunk.setName("My PSTN Trunk " + suffix);
            pstnTrunk.setRingingTimeout(30);

            PstnConfiguration pstn = new PstnConfiguration();
            pstn.setDst("12125551234");
            pstnTrunk.setConfiguration(pstn);

            VoiceInTrunk createdPstn = client.voiceInTrunks().create(pstnTrunk).getData();
            pstnId = createdPstn.getId();
            System.out.println("Created PSTN trunk: " + createdPstn.getId() + " - " + createdPstn.getName());
            System.out.println("  Config type: " + createdPstn.getConfiguration().getType());
            System.out.println("  DST: " + ((PstnConfiguration) createdPstn.getConfiguration()).getDst());

            // --- Find the SIP trunk by ID, with its trunk group ---
            QueryParams findParams = QueryParams.builder()
                    .include("voice_in_trunk_group")
                    .build();
            VoiceInTrunk found = client.voiceInTrunks().find(sipId, findParams).getData();
            System.out.println("\nFound trunk:");
            printTrunk(found);

            // --- Update the SIP trunk: only the changed description is sent ---
            found.setDescription("Updated description");
            VoiceInTrunk updated = client.voiceInTrunks().update(found).getData();
            System.out.println("\nUpdated SIP trunk description: " + updated.getDescription());

            // --- Update the PSTN trunk: the configuration type must stay the same ---
            PstnConfiguration newDst = new PstnConfiguration();
            newDst.setDst("12125556789");
            VoiceInTrunk pstnUpdate = new VoiceInTrunk().withId(pstnId);
            pstnUpdate.setName("My PSTN Trunk " + suffix + " (updated)");
            pstnUpdate.setConfiguration(newDst);
            VoiceInTrunk updatedPstn = client.voiceInTrunks().update(pstnUpdate).getData();
            System.out.println("Updated PSTN trunk: " + updatedPstn.getName()
                    + " -> " + ((PstnConfiguration) updatedPstn.getConfiguration()).getDst());
        } finally {
            try {
                if (sipId != null) {
                    client.voiceInTrunks().delete(sipId);
                    System.out.println("\nSIP trunk deleted");
                }
            } finally {
                if (pstnId != null) {
                    client.voiceInTrunks().delete(pstnId);
                    System.out.println("PSTN trunk deleted");
                }
            }
        }
    }

    private static void printTrunk(VoiceInTrunk trunk) {
        String configType = trunk.getConfiguration() != null ? trunk.getConfiguration().getType() : "unknown";
        String group = trunk.getVoiceInTrunkGroup() != null ? trunk.getVoiceInTrunkGroup().getName() : "none";
        System.out.println(trunk.getName() + " [" + configType + "] pop=" + trunk.getPop() + " group=" + group);
        if (trunk.getConfiguration() instanceof SipConfiguration) {
            SipConfiguration sip = (SipConfiguration) trunk.getConfiguration();
            System.out.println("  host=" + sip.getHost() + " codecs=" + sip.getCodecs() + " ice=" + sip.getIceMode());
        } else if (trunk.getConfiguration() instanceof PstnConfiguration) {
            System.out.println("  dst=" + ((PstnConfiguration) trunk.getConfiguration()).getDst());
        }
    }
}
