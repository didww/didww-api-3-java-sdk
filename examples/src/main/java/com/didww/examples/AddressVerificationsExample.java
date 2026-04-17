package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.AddressVerification;

import java.util.List;

/**
 * Lists and inspects address verifications (2026-04-16).
 *
 * An AddressVerification ties an address to one or more DIDs for
 * regulatory compliance. Each record has a status (Pending, Approved, Rejected),
 * optional reject reasons/comment, and an external_reference_id.
 *
 * Supported operations: index, show, create, update (external_reference_id).
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.AddressVerificationsExample
 */
public class AddressVerificationsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        System.out.println("=== Address Verifications ===");
        QueryParams params = QueryParams.builder()
                .include("address", "dids")
                .build();
        List<AddressVerification> verifications = client.addressVerifications().list(params).getData();
        System.out.println("Found " + verifications.size() + " address verifications");

        int limit = Math.min(5, verifications.size());
        for (int i = 0; i < limit; i++) {
            AddressVerification v = verifications.get(i);
            System.out.println("\nVerification: " + v.getId());
            System.out.println("  Reference: " + v.getReference());
            System.out.println("  Status: " + v.getStatus());
            System.out.println("  isPending: " + v.isPending());
            System.out.println("  isApproved: " + v.isApproved());
            System.out.println("  isRejected: " + v.isRejected());
            System.out.println("  External Reference: " + v.getExternalReferenceId());
            System.out.println("  Created: " + v.getCreatedAt());
            if (v.getRejectReasons() != null && !v.getRejectReasons().isEmpty()) {
                System.out.println("  Reject reasons: " + String.join(", ", v.getRejectReasons()));
            }
            if (v.getRejectComment() != null) {
                System.out.println("  Reject comment: " + v.getRejectComment());
            }
            if (v.getAddress() != null) {
                System.out.println("  Address city: " + v.getAddress().getCityName());
            }
        }
    }
}
