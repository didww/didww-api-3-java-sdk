package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.DidHistory;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Lists DID ownership history (2026-04-16).
 * Records are retained for the last 90 days only.
 *
 * Server-side filters supported:
 *   did_number (eq), action (eq), method (eq),
 *   created_at_gteq, created_at_lteq
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.DidHistoryExample
 */
public class DidHistoryExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // List most recent DID history events
        System.out.println("=== Recent DID History ===");
        List<DidHistory> events = client.didHistory().list().getData();
        System.out.println("Found " + events.size() + " events in the last 90 days");

        int limit = Math.min(10, events.size());
        for (int i = 0; i < limit; i++) {
            DidHistory event = events.get(i);
            System.out.printf("%s  %-16s  %-28s  via %s%n",
                    event.getCreatedAt(), event.getDidNumber(), event.getAction(), event.getMethod());
        }

        // Filter by action
        System.out.println("\n=== Only 'assigned' events ===");
        QueryParams assignedParams = QueryParams.builder()
                .filter("action", "assigned")
                .build();
        List<DidHistory> assigned = client.didHistory().list(assignedParams).getData();
        System.out.println("Found " + assigned.size() + " assignments");

        // Filter by a specific DID number
        if (!events.isEmpty()) {
            String number = events.get(0).getDidNumber();
            System.out.println("\n=== History for DID " + number + " ===");
            QueryParams numberParams = QueryParams.builder()
                    .filter("did_number", number)
                    .build();
            List<DidHistory> perNumber = client.didHistory().list(numberParams).getData();
            for (DidHistory event : perNumber) {
                System.out.printf("%s  %s  via %s%n",
                        event.getCreatedAt(), event.getAction(), event.getMethod());
            }
        }

        // Filter by date range (last 7 days)
        String sevenDaysAgo = OffsetDateTime.now().minus(7, ChronoUnit.DAYS).toString();
        System.out.println("\n=== History since " + sevenDaysAgo + " ===");
        QueryParams recentParams = QueryParams.builder()
                .filter("created_at_gteq", sevenDaysAgo)
                .build();
        List<DidHistory> recent = client.didHistory().list(recentParams).getData();
        System.out.println("Found " + recent.size() + " events in the last 7 days");
    }
}
