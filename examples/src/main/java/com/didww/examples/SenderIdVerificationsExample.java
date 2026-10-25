package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.SenderIdVerification;
import com.didww.sdk.resource.SmsTrunk;
import com.didww.sdk.resource.enums.SenderIdVerificationStatus;

import java.util.Collections;
import java.util.List;

/**
 * Lists Sender ID verifications (2026-10-22) with their SMS trunks, renames an active one and routes its
 * outbound messages through an SMS trunk that sends them, then restores its original name and SMS trunk,
 * also when a step fails.
 *
 * A verification is created and reviewed outside the API; only its name and its SMS trunk can be changed,
 * and a closed one cannot be changed at all. Only http_out, smpp_esme and smpp_smsc trunks are accepted.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.SenderIdVerificationsExample
 */
public class SenderIdVerificationsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        System.out.println("=== Sender ID Verifications ===");
        SenderIdVerification verification = listVerifications(client);
        if (verification == null) {
            System.out.println("No active Sender ID verification");
            return;
        }

        String id = verification.getId();
        String originalName = verification.getName();
        String originalTrunkId = verification.getSmsTrunk() != null ? verification.getSmsTrunk().getId() : null;

        System.out.println("\n=== Renaming " + verification.getReference() + " ===");
        try {
            SenderIdVerification rename = new SenderIdVerification().withId(id);
            rename.setName(originalName + " (renamed)");
            System.out.println("Name: " + client.senderIdVerifications().update(rename).getData().getName());
        } finally {
            SenderIdVerification restore = new SenderIdVerification().withId(id);
            restore.setName(originalName);
            client.senderIdVerifications().update(restore);
            System.out.println("Name restored: " + originalName);
        }

        SmsTrunk trunk = findSendingTrunk(client);
        if (trunk == null) {
            System.out.println("No SMS trunk that sends messages");
            return;
        }

        System.out.println("\n=== Routing " + verification.getReference() + " through " + trunk.getName() + " ===");
        try {
            SenderIdVerification route = new SenderIdVerification().withId(id);
            route.setSmsTrunk(new SmsTrunk().withId(trunk.getId()));
            client.senderIdVerifications().update(route);
            printRoutedThrough(client, trunk.getId());
        } finally {
            // Restore the original trunk even when a step above fails. Null unassigns the trunk,
            // which stops outbound messages until a trunk is assigned again.
            System.out.println("\n=== Restoring the original SMS trunk ===");
            SenderIdVerification restore = new SenderIdVerification().withId(id);
            restore.setSmsTrunk(originalTrunkId != null ? new SmsTrunk().withId(originalTrunkId) : null);
            client.senderIdVerifications().update(restore);
            System.out.println(originalTrunkId != null
                    ? "Routed through " + verification.getSmsTrunk().getName() + " again"
                    : "No SMS trunk again");
        }
    }

    // Prints the verifications and returns the first active one.
    private static SenderIdVerification listVerifications(DidwwClient client) {
        QueryParams params = QueryParams.builder()
                .include("sms_trunk")
                .build();
        List<SenderIdVerification> verifications = client.senderIdVerifications().list(params).getData();
        SenderIdVerification active = null;
        for (SenderIdVerification v : verifications) {
            printVerification(v);
            if (active == null && v.isActive()) {
                active = v;
            }
        }
        return active;
    }

    private static void printVerification(SenderIdVerification v) {
        String routedThrough = v.getSmsTrunk() != null ? v.getSmsTrunk().getName() : "no SMS trunk";
        List<String> senderIds = v.getSenderIds() != null ? v.getSenderIds() : Collections.emptyList();
        System.out.println(v.getReference() + " " + v.getName() + " (" + v.getSenderType() + ", " + v.getStatus()
                + "): " + String.join(", ", senderIds) + " via " + routedThrough);
        if (v.isChangesRequired()) {
            List<String> reasons = v.getRejectReasons() != null ? v.getRejectReasons() : Collections.emptyList();
            System.out.println("  changes required: " + String.join("; ", reasons) + " "
                    + (v.getRejectComment() != null ? v.getRejectComment() : ""));
        }
        // Prices are decimal strings; the renew date is "YYYY-MM-DD", null once closed
        System.out.println("  setup price: " + v.getMetaSetupPrice() + ", monthly price: " + v.getMetaMonthlyPrice()
                + ", renews: " + v.getRenewDate());
    }

    // Returns the first SMS trunk that sends messages.
    private static SmsTrunk findSendingTrunk(DidwwClient client) {
        for (SmsTrunk trunk : client.smsTrunks().list().getData()) {
            if (Boolean.TRUE.equals(trunk.getAllowOut())) {
                return trunk;
            }
        }
        return null;
    }

    private static void printRoutedThrough(DidwwClient client, String trunkId) {
        System.out.println("\nActive verifications routed through this trunk:");
        QueryParams filtered = QueryParams.builder()
                .filter("status", EnumWire.value(SenderIdVerificationStatus.ACTIVE))
                .filter("sms_trunk.id", trunkId)
                .build();
        for (SenderIdVerification v : client.senderIdVerifications().list(filtered).getData()) {
            System.out.println("  " + v.getReference() + " " + v.getName());
        }
    }
}
