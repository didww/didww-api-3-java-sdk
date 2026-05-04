package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.VoiceInTrunk;
import com.didww.sdk.resource.configuration.SipConfiguration;
import com.didww.sdk.resource.enums.CliFormat;
import com.didww.sdk.resource.enums.Codec;
import com.didww.sdk.resource.enums.TransportProtocol;

import java.util.Arrays;

/**
 * End-to-end SIP registration flow on /voice_in_trunks (API 2026-04-16):
 * create with sip_registration enabled → rename → disable by setting host
 * → re-enable by toggling the flag. Demonstrates how the SDK keeps the
 * dependent fields (host, port, useDidInRuri) aligned with the server's
 * validation rules. The sandbox trunk is left in place after the script
 * completes.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.VoiceInTrunkSipRegistrationExample
 */
public class VoiceInTrunkSipRegistrationExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        System.out.println("=== Java SDK — SIP registration flow ===");

        // 1) Create with sip_registration enabled.
        System.out.println("\n[1/4] Create with sip_registration enabled...");
        SipConfiguration sip = new SipConfiguration();
        sip.setEnabledSipRegistration(true);
        sip.setUseDidInRuri(true);
        sip.setCnamLookup(false);
        sip.setCodecIds(Arrays.asList(Codec.PCMU, Codec.PCMA));
        sip.setTransportProtocolId(TransportProtocol.UDP);

        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setName("java-sip-registration-" + System.currentTimeMillis());
        trunk.setPriority(1);
        trunk.setWeight(100);
        trunk.setCliFormat(CliFormat.E164);
        trunk.setRingingTimeout(30);
        trunk.setConfiguration(sip);

        VoiceInTrunk created = client.voiceInTrunks().create(trunk).getData();
        String trunkId = created.getId();
        SipConfiguration cfg1 = (SipConfiguration) created.getConfiguration();
        System.out.println("  id=" + trunkId);
        System.out.println("  incomingAuthUsername=" + cfg1.getIncomingAuthUsername());
        System.out.println("  incomingAuthPassword=" + cfg1.getIncomingAuthPassword());

        // 2) Rename — single-field PATCH.
        System.out.println("\n[2/4] Rename trunk...");
        VoiceInTrunk rename = new VoiceInTrunk().withId(trunkId);
        rename.setName("java-renamed-" + System.currentTimeMillis());
        client.voiceInTrunks().update(rename);
        System.out.println("  name=" + rename.getName());

        // 3) Disable sip_registration by setting host.
        System.out.println("\n[3/4] Disable by setting host...");
        SipConfiguration disable = new SipConfiguration();
        disable.setHost("203.0.113.10");
        VoiceInTrunk update = new VoiceInTrunk().withId(trunkId);
        update.setConfiguration(disable);
        client.voiceInTrunks().update(update);
        SipConfiguration cfg3 = (SipConfiguration) client.voiceInTrunks().find(trunkId).getData().getConfiguration();
        System.out.println("  enabledSipRegistration=" + cfg3.getEnabledSipRegistration());
        System.out.println("  useDidInRuri=" + cfg3.getUseDidInRuri());
        System.out.println("  host=" + cfg3.getHost());
        System.out.println("  incomingAuthUsername=" + cfg3.getIncomingAuthUsername());

        // 4) Re-enable sip_registration. The SDK should send host=null / port=null
        //    on the wire so the server clears the values it had persisted.
        System.out.println("\n[4/4] Re-enable by toggling enabledSipRegistration...");
        SipConfiguration reEnable = new SipConfiguration();
        reEnable.setEnabledSipRegistration(true);
        reEnable.setUseDidInRuri(true);
        VoiceInTrunk update4 = new VoiceInTrunk().withId(trunkId);
        update4.setConfiguration(reEnable);
        try {
            client.voiceInTrunks().update(update4);
            SipConfiguration cfg4 = (SipConfiguration) client.voiceInTrunks().find(trunkId).getData().getConfiguration();
            System.out.println("  enabledSipRegistration=" + cfg4.getEnabledSipRegistration());
            System.out.println("  host=" + cfg4.getHost());
            System.out.println("  incomingAuthUsername=" + cfg4.getIncomingAuthUsername());
            System.out.println("\n=== PASS — trunk " + trunkId + " left in sandbox ===");
        } catch (Exception e) {
            System.out.println("  ✗ FAIL: " + e.getMessage());
            System.out.println("\n=== FAIL at re-enable — trunk " + trunkId + " left in sandbox ===");
        }
    }
}
