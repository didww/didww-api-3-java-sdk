package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.EmergencyVerification;

import java.util.List;

/**
 * Lists and creates emergency verifications (2026-04-16).
 *
 * An EmergencyVerification validates that a customer's address and DIDs
 * meet the requirements for an emergency calling service. Each record
 * has a status (pending, approved, rejected) and optional reject reasons.
 *
 * Supported operations: index, show, create, update (external_reference_id).
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.EmergencyVerificationsExample
 */
public class EmergencyVerificationsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        System.out.println("=== Emergency Verifications ===");
        QueryParams params = QueryParams.builder()
                .include("address", "emergency_calling_service", "dids")
                .build();
        List<EmergencyVerification> verifications = client.emergencyVerifications().list(params).getData();
        System.out.println("Found " + verifications.size() + " emergency verifications");

        int limit = Math.min(5, verifications.size());
        for (int i = 0; i < limit; i++) {
            EmergencyVerification v = verifications.get(i);
            System.out.println("\nVerification: " + v.getId());
            System.out.println("  Reference: " + v.getReference());
            System.out.println("  Status: " + v.getStatus());
            System.out.println("  External Reference: " + v.getExternalReferenceId());
            System.out.println("  Created: " + v.getCreatedAt());
            if (v.getRejectReasons() != null && !v.getRejectReasons().isEmpty()) {
                System.out.println("  Reject reasons: " + String.join(", ", v.getRejectReasons()));
            }
            if (v.getRejectComment() != null) {
                System.out.println("  Reject comment: " + v.getRejectComment());
            }
            System.out.println("  Callback URL: " + v.getCallbackUrl());
            System.out.println("  Callback Method: " + v.getCallbackMethod());
        }

        // Find a specific verification
        if (!verifications.isEmpty()) {
            System.out.println("\n=== Specific Emergency Verification ===");
            EmergencyVerification specific = client.emergencyVerifications()
                    .find(verifications.get(0).getId()).getData();
            System.out.println("Verification: " + specific.getId());
            System.out.println("  Status: " + specific.getStatus());
            System.out.println("  isPending: " + specific.isPending());
            System.out.println("  isApproved: " + specific.isApproved());
            System.out.println("  isRejected: " + specific.isRejected());
        }
    }
}
