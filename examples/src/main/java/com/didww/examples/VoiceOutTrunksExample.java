package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.VoiceOutTrunk;
import com.didww.sdk.resource.enums.DefaultDstAction;
import com.didww.sdk.resource.enums.OnCliMismatchAction;

import java.util.Arrays;
import java.util.List;

/**
 * Voice Out Trunks: CRUD operations.
 * Note: Voice Out Trunks and some OnCliMismatchAction values (e.g. REPLACE_CLI, RANDOMIZE_CLI)
 * require additional account configuration. Contact DIDWW support to enable.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.VoiceOutTrunksExample
 */
public class VoiceOutTrunksExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Create a voice out trunk
        VoiceOutTrunk trunk = new VoiceOutTrunk();
        trunk.setName("My Outbound Trunk");
        trunk.setAllowedSipIps(Arrays.asList("0.0.0.0/0"));
        trunk.setDefaultDstAction(DefaultDstAction.ALLOW_ALL);
        trunk.setOnCliMismatchAction(OnCliMismatchAction.REJECT_CALL);

        VoiceOutTrunk created = client.voiceOutTrunks().create(trunk).getData();
        System.out.println("Created voice out trunk: " + created.getId());
        System.out.println("  name: " + created.getName());
        System.out.println("  username: " + created.getUsername());
        System.out.println("  password: " + created.getPassword());
        System.out.println("  status: " + created.getStatus());

        // List voice out trunks
        List<VoiceOutTrunk> trunks = client.voiceOutTrunks().list(null).getData();
        System.out.println("\nAll voice out trunks (" + trunks.size() + "):");
        for (VoiceOutTrunk t : trunks) {
            System.out.println("  " + t.getName() + " (" + t.getStatus() + ")");
        }

        // Update
        created.setName("Updated Outbound Trunk");
        created.setAllowedSipIps(Arrays.asList("10.0.0.0/8"));
        VoiceOutTrunk updated = client.voiceOutTrunks().update(created).getData();
        System.out.println("\nUpdated name: " + updated.getName());

        // Delete
        client.voiceOutTrunks().delete(created.getId());
        System.out.println("Deleted voice out trunk");
    }
}
