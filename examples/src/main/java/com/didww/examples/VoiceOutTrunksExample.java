package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.VoiceOutTrunk;
import com.didww.sdk.resource.authenticationmethod.AuthenticationMethod;
import com.didww.sdk.resource.authenticationmethod.CredentialsAndIpAuthenticationMethod;
import com.didww.sdk.resource.authenticationmethod.IpOnlyAuthenticationMethod;
import com.didww.sdk.resource.authenticationmethod.TwilioAuthenticationMethod;
import com.didww.sdk.resource.enums.DefaultDstAction;
import com.didww.sdk.resource.enums.OnCliMismatchAction;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Voice Out Trunks: CRUD operations using 2026-04-16 polymorphic authentication_method.
 * Note: Voice Out Trunks require additional account configuration.
 * Contact DIDWW support to enable.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.VoiceOutTrunksExample
 */
public class VoiceOutTrunksExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        // List voice out trunks
        List<VoiceOutTrunk> trunks = client.voiceOutTrunks().list(null).getData();
        System.out.println("Found " + trunks.size() + " voice out trunks");
        for (VoiceOutTrunk t : trunks) {
            System.out.println(t.getName() + " (" + t.getStatus() + ")");
            System.out.println("  ID: " + t.getId());
            AuthenticationMethod auth = t.getAuthenticationMethod();
            System.out.println("  Auth type: " + (auth != null ? auth.getType() : "null"));
            if (auth instanceof CredentialsAndIpAuthenticationMethod) {
                System.out.println("  Username: " + ((CredentialsAndIpAuthenticationMethod) auth).getUsername());
            } else if (auth instanceof IpOnlyAuthenticationMethod) {
                System.out.println("  Allowed SIP IPs: " + ((IpOnlyAuthenticationMethod) auth).getAllowedSipIps());
            } else if (auth instanceof TwilioAuthenticationMethod) {
                System.out.println("  Twilio Account SID: " + ((TwilioAuthenticationMethod) auth).getTwilioAccountSid());
            }
            System.out.println("  Default DST Action: " + t.getDefaultDstAction());
            System.out.println("  On CLI Mismatch: " + t.getOnCliMismatchAction());
            System.out.println("  External Reference ID: " + t.getExternalReferenceId());
            System.out.println("  Emergency Enable All: " + t.getEmergencyEnableAll());
            System.out.println("  RTP Timeout: " + t.getRtpTimeout());
            System.out.println();
        }

        // Create a voice out trunk with credentials_and_ip authentication
        // Note: ip_only authentication is read-only and can only be configured by DIDWW staff.
        System.out.println("\n=== Creating Voice Out Trunk (credentials_and_ip) ===");
        CredentialsAndIpAuthenticationMethod createAuth = new CredentialsAndIpAuthenticationMethod();
        createAuth.setAllowedSipIps(Collections.singletonList("203.0.113.0/24"));
        createAuth.setTechPrefix("");

        VoiceOutTrunk trunk = new VoiceOutTrunk();
        trunk.setName("Java Outbound Trunk " + uniqueSuffix);
        trunk.setAuthenticationMethod(createAuth);
        trunk.setDefaultDstAction(DefaultDstAction.ALLOW_ALL);
        trunk.setOnCliMismatchAction(OnCliMismatchAction.REJECT_CALL);
        trunk.setExternalReferenceId("java-example-" + uniqueSuffix);
        trunk.setRtpTimeout(60);

        VoiceOutTrunk created = client.voiceOutTrunks().create(trunk).getData();
        System.out.println("Created voice out trunk: " + created.getId());
        System.out.println("  Name: " + created.getName());
        System.out.println("  Auth type: " + created.getAuthenticationMethod().getType());
        System.out.println("  Status: " + created.getStatus());
        System.out.println("  External Reference: " + created.getExternalReferenceId());

        // Update trunk - change allowed IPs and tech prefix
        System.out.println("\n=== Updating Voice Out Trunk ===");
        CredentialsAndIpAuthenticationMethod updateAuth = new CredentialsAndIpAuthenticationMethod();
        updateAuth.setAllowedSipIps(Collections.singletonList("203.0.113.0/24"));
        updateAuth.setTechPrefix("9");

        created.setName("Updated Outbound Trunk " + uniqueSuffix);
        created.setAuthenticationMethod(updateAuth);

        VoiceOutTrunk updated = client.voiceOutTrunks().update(created).getData();
        System.out.println("Updated name: " + updated.getName());
        System.out.println("  New auth type: " + updated.getAuthenticationMethod().getType());
        if (updated.getAuthenticationMethod() instanceof CredentialsAndIpAuthenticationMethod) {
            System.out.println("  Username: " + ((CredentialsAndIpAuthenticationMethod) updated.getAuthenticationMethod()).getUsername());
        }

        // Delete
        System.out.println("\n=== Deleting Voice Out Trunk ===");
        client.voiceOutTrunks().delete(created.getId());
        System.out.println("Deleted voice out trunk");
    }
}
