package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.Export;
import com.didww.sdk.resource.enums.ExportType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creates and lists CDR exports (cdr_in / cdr_out).
 *
 * 2026-04-16 additions:
 *   - external_reference_id: customer-supplied reference (max 100 chars)
 *
 * Filter semantics on CDR exports:
 *   - filters.from: lower bound, INCLUSIVE  (server: time_start &gt;= from)
 *   - filters.to:   upper bound, EXCLUSIVE  (server: time_start &lt;  to)
 * To cover a whole day, pass from: "2026-04-15 00:00:00", to: "2026-04-16 00:00:00".
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.ExportsExample
 */
public class ExportsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        // List existing exports
        System.out.println("=== Existing Exports ===");
        List<Export> exports = client.exports().list(null).getData();
        System.out.println("Found " + exports.size() + " exports");

        int limit = Math.min(5, exports.size());
        for (int i = 0; i < limit; i++) {
            Export e = exports.get(i);
            System.out.println("Export: " + e.getId());
            System.out.println("  Type: " + e.getExportType());
            System.out.println("  Status: " + e.getStatus());
            System.out.println("  Created: " + e.getCreatedAt());
            if (e.getUrl() != null) {
                System.out.println("  URL: " + e.getUrl());
            }
            if (e.getCallbackUrl() != null) {
                System.out.println("  Callback URL: " + e.getCallbackUrl());
            }
            if (e.getExternalReferenceId() != null) {
                System.out.println("  External Reference: " + e.getExternalReferenceId());
            }
            System.out.println();
        }

        // Create a CDR-In export for yesterday (from is inclusive, to is exclusive)
        System.out.println("\n=== Creating CDR-In Export (yesterday, with external_reference_id) ===");
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Map<String, Object> filters = new HashMap<>();
        filters.put("from", yesterday + " 00:00:00"); // inclusive (time_start >= this)
        filters.put("to", today + " 00:00:00");        // exclusive (time_start < this)

        Export export = new Export();
        export.setExportType(ExportType.CDR_IN);
        export.setFilters(filters);
        export.setExternalReferenceId("java-cdr-in-" + uniqueSuffix);

        Export created = client.exports().create(export).getData();
        System.out.println("Created CDR-In export: " + created.getId());
        System.out.println("  External Reference: " + created.getExternalReferenceId());
        System.out.println("  Status: " + created.getStatus());

        // Find and inspect a specific export
        if (!exports.isEmpty()) {
            System.out.println("\n=== Specific Export Details ===");
            Export specific = client.exports().find(exports.get(0).getId()).getData();
            System.out.println("Export: " + specific.getId());
            System.out.println("  Type: " + specific.getExportType());
            System.out.println("  Status: " + specific.getStatus());
            System.out.println("  External Reference: " + specific.getExternalReferenceId());
        }
    }
}
