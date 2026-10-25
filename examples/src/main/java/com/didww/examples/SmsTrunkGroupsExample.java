package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.SmsTrunk;
import com.didww.sdk.resource.SmsTrunkGroup;
import com.didww.sdk.resource.enums.SmsHttpMethod;
import com.didww.sdk.resource.smstrunkconfiguration.HttpInConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmsToEmailConfiguration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD for SMS trunk groups (2026-10-22): creates two inbound SMS trunks, puts them in a group,
 * lists groups, and deletes the group.
 *
 * A group spreads a DID's inbound messages over its trunks by their priority (higher first).
 * http_out trunks cannot join a group. Deleting a group also deletes the trunks in it.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.SmsTrunkGroupsExample
 */
public class SmsTrunkGroupsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        String primaryId = null;
        String fallbackId = null;
        String groupId = null;
        try {
            System.out.println("=== Creating SMS Trunks ===");
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("text", "{SMS_TEXT}");
            query.put("from", "{SMS_SRC_ADDR}");
            HttpInConfiguration http = new HttpInConfiguration();
            http.setUrl("https://example.com/sms");
            http.setHttpMethod(SmsHttpMethod.GET); // GET takes no body type and no body
            http.setQueryParameters(query);

            SmsTrunk primary = new SmsTrunk();
            primary.setName("Java primary " + suffix);
            primary.setPriority(200);
            primary.setConfiguration(http);
            primaryId = client.smsTrunks().create(primary).getData().getId();
            System.out.println("Created SMS trunk: " + primaryId + " - " + primary.getName());

            SmsToEmailConfiguration email = new SmsToEmailConfiguration();
            email.setSendTo("sms@example.com");
            email.setSubject("Inbound SMS");
            email.setMessage("You have a new message.");

            SmsTrunk fallback = new SmsTrunk();
            fallback.setName("Java fallback " + suffix);
            fallback.setPriority(100);
            fallback.setConfiguration(email);
            fallbackId = client.smsTrunks().create(fallback).getData().getId();
            System.out.println("Created SMS trunk: " + fallbackId + " - " + fallback.getName());

            System.out.println("\n=== Creating SMS Trunk Group ===");
            SmsTrunkGroup group = new SmsTrunkGroup();
            group.setName("Java SMS Group " + suffix);
            group.setExternalReferenceId("java-sms-group-" + suffix);
            // The list replaces the group's membership
            group.setSmsTrunks(Arrays.asList(new SmsTrunk().withId(primaryId), new SmsTrunk().withId(fallbackId)));
            SmsTrunkGroup created = client.smsTrunkGroups().create(group).getData();
            groupId = created.getId();
            System.out.println("Created SMS trunk group: " + created.getId() + " - " + created.getName()
                    + " (" + created.getMetaSmsTrunksCount() + " trunks)");

            System.out.println("\n=== Existing SMS Trunk Groups ===");
            QueryParams params = QueryParams.builder()
                    .include("sms_trunks")
                    .page(1, 5)
                    .build();
            List<SmsTrunkGroup> groups = client.smsTrunkGroups().list(params).getData();
            for (SmsTrunkGroup g : groups) {
                String names = g.getSmsTrunks() == null ? ""
                        : g.getSmsTrunks().stream().map(SmsTrunk::getName).collect(Collectors.joining(", "));
                System.out.println(g.getName() + " (" + g.getMetaSmsTrunksCount() + " trunks) " + names);
            }
        } finally {
            if (groupId != null) {
                System.out.println("\n=== Deleting SMS Trunk Group (and its trunks) ===");
                client.smsTrunkGroups().delete(groupId);
                System.out.println("SMS trunk group deleted");
            } else {
                // Without a group the trunks are deleted one by one
                try {
                    if (primaryId != null) {
                        client.smsTrunks().delete(primaryId);
                        System.out.println("SMS trunk deleted: " + primaryId);
                    }
                } finally {
                    if (fallbackId != null) {
                        client.smsTrunks().delete(fallbackId);
                        System.out.println("SMS trunk deleted: " + fallbackId);
                    }
                }
            }
        }
    }
}
