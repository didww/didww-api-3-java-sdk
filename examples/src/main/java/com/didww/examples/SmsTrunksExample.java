package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.SmsTrunk;
import com.didww.sdk.resource.SmsTrunkRegenerateCredential;
import com.didww.sdk.resource.enums.SmsBodyType;
import com.didww.sdk.resource.enums.SmsHttpMethod;
import com.didww.sdk.resource.smstrunkconfiguration.HttpInConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.HttpOutConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * CRUD for SMS trunks (2026-10-22): lists trunks, creates an HTTP IN trunk, updates its configuration,
 * regenerates the credentials of an HTTP OUT trunk, and deletes both trunks.
 *
 * Configuration types (com.didww.sdk.resource.smstrunkconfiguration):
 *   - SmsToEmailConfiguration, HttpInConfiguration: DIDWW delivers inbound messages by email or HTTP
 *   - HttpOutConfiguration, SmppEsmeConfiguration: you connect to DIDWW; DIDWW generates the
 *     credentials, which are read-only
 *   - SmppSmscConfiguration: DIDWW binds to your SMSC
 *
 * The read-only allowIn / allowOut tell which directions a trunk handles. Printing a configuration
 * masks its credentials.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.SmsTrunksExample
 */
public class SmsTrunksExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        listTrunks(client);

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        System.out.println("\n=== Creating HTTP IN Trunk ===");
        HttpInConfiguration config = new HttpInConfiguration();
        config.setUrl("https://example.com/sms");
        // POST and PUT need a body type and a body; GET takes neither.
        config.setHttpMethod(SmsHttpMethod.POST);
        config.setBodyType(SmsBodyType.JSON);
        // Placeholders: {SMS_TEXT}, {SMS_TEXT_BASE64_ENCODED}, {SMS_SRC_ADDR}, {SMS_DST_ADDR}, {SMS_TIME}
        config.setBody("{\"text\":\"{SMS_TEXT}\",\"from\":\"{SMS_SRC_ADDR}\",\"to\":\"{SMS_DST_ADDR}\"}");
        config.setValidateHttpsCertificate(true);

        SmsTrunk trunk = new SmsTrunk();
        trunk.setName("Java HTTP IN " + suffix);
        trunk.setExternalReferenceId("java-sms-" + suffix);
        trunk.setConfiguration(config);

        String createdId = null;
        try {
            SmsTrunk created = client.smsTrunks().create(trunk).getData();
            createdId = created.getId();
            System.out.println("Created SMS trunk: " + created.getId() + " - " + created.getName());
            System.out.println("  Configuration: " + created.getConfiguration());

            // The configuration type cannot change; attributes left out keep their values.
            System.out.println("\n=== Updating HTTP IN Trunk ===");
            HttpInConfiguration newConfig = new HttpInConfiguration();
            newConfig.setUrl("https://example.com/sms-v2");
            SmsTrunk update = new SmsTrunk().withId(created.getId());
            update.setName("Java HTTP IN " + suffix + " (v2)");
            update.setConfiguration(newConfig);
            SmsTrunk updated = client.smsTrunks().update(update).getData();
            if (updated.getConfiguration() instanceof HttpInConfiguration) {
                System.out.println("Updated: " + updated.getName() + " -> "
                        + ((HttpInConfiguration) updated.getConfiguration()).getUrl());
            }

            httpOutRoundTrip(client, suffix);
        } finally {
            if (createdId != null) {
                System.out.println("\n=== Deleting HTTP IN Trunk ===");
                client.smsTrunks().delete(createdId);
                System.out.println("SMS trunk deleted");
            }
        }
    }

    private static void listTrunks(DidwwClient client) {
        System.out.println("=== Existing SMS Trunks ===");
        QueryParams params = QueryParams.builder()
                .include("sms_trunk_group")
                .page(1, 10)
                .build();
        List<SmsTrunk> trunks = client.smsTrunks().list(params).getData();
        System.out.println("Found " + trunks.size() + " SMS trunks");
        for (SmsTrunk trunk : trunks) {
            String type = trunk.getConfiguration() != null ? trunk.getConfiguration().getType() : "none";
            System.out.println(trunk.getName() + " [" + type + "]");
            System.out.println("  ID: " + trunk.getId());
            System.out.println("  Inbound: " + trunk.getAllowIn() + ", outbound: " + trunk.getAllowOut());
            if (trunk.getSmsTrunkGroup() != null) {
                System.out.println("  Group: " + trunk.getSmsTrunkGroup().getName());
            }
            if (trunk.getMetaHost() != null) {
                System.out.println("  Submit URL: " + trunk.getMetaHost());
            }
        }
    }

    // Creates an HTTP OUT trunk, regenerates its credentials and deletes it. The generated credentials of
    // http_out and smpp_esme trunks are rotated with an SmsTrunkRegenerateCredential and read back from the trunk.
    private static void httpOutRoundTrip(DidwwClient client, String suffix) {
        System.out.println("\n=== Creating HTTP OUT Trunk and Regenerating Its Credentials ===");
        HttpOutConfiguration config = new HttpOutConfiguration();
        config.setCallbackUrl("https://example.com/sms/status");
        config.setAllowedIps(Collections.singletonList("203.0.113.10"));

        SmsTrunk trunk = new SmsTrunk();
        trunk.setName("Java HTTP OUT " + suffix);
        trunk.setConfiguration(config);

        String trunkId = null;
        try {
            trunkId = client.smsTrunks().create(trunk).getData().getId();
            System.out.println("Created SMS trunk: " + trunkId);

            SmsTrunkRegenerateCredential regenerate = new SmsTrunkRegenerateCredential();
            regenerate.setSmsTrunk(new SmsTrunk().withId(trunkId));
            client.smsTrunkRegenerateCredentials().create(regenerate);

            // The response carries no credentials: read the trunk again.
            SmsTrunk reloaded = client.smsTrunks().find(trunkId).getData();
            if (reloaded.getConfiguration() instanceof HttpOutConfiguration) {
                HttpOutConfiguration credentials = (HttpOutConfiguration) reloaded.getConfiguration();
                // Use credentials.getAuthName() and credentials.getAuthPassword() to submit messages to
                // reloaded.getMetaHost(). Printing the configuration masks them.
                System.out.println("Credentials regenerated: " + credentials);
                System.out.println("  Submit URL: " + reloaded.getMetaHost());
            }
        } finally {
            if (trunkId != null) {
                client.smsTrunks().delete(trunkId);
                System.out.println("HTTP OUT trunk deleted");
            }
        }
    }
}
