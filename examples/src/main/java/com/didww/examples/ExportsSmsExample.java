package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Export;
import com.didww.sdk.resource.enums.ExportStatus;
import com.didww.sdk.resource.enums.ExportTrunkType;
import com.didww.sdk.resource.enums.ExportType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

/**
 * Creates SMS exports (sms_in / sms_out, 2026-10-22) for yesterday and reads the columns of a completed
 * SMS-Out export.
 *
 * Filters on SMS exports (Export#setFilters):
 *   - "from", "to" (required): "YYYY-MM-DD HH:MM:SS" in UTC; from inclusive, to exclusive and not in the future
 *   - "source_address_eq" / "source_address_cont", "destination_address_eq" / "destination_address_cont",
 *     "status", "sms_id"
 *   - "sms_trunk.id": one id or a list of ids
 *   - sms_in only: "trunk_type", one or a list of ExportTrunkType values in their wire form
 *   - sms_out only: "service_type" ("a2p" or "p2p"), "sender_id_verification.id"
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.ExportsSmsExample
 */
public class ExportsSmsExample {

    public static void main(String[] args) throws IOException {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Both bounds come from one UTC midnight, so "to" is never in the future
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String from = today.minusDays(1) + " 00:00:00"; // inclusive
        String to = today + " 00:00:00";                // exclusive
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        System.out.println("=== Creating SMS-Out Export (A2P, yesterday) ===");
        Map<String, Object> outFilters = new HashMap<>();
        outFilters.put("from", from);
        outFilters.put("to", to);
        outFilters.put("service_type", "a2p");
        create(client, ExportType.SMS_OUT, outFilters, "java-sms-out-" + suffix);

        System.out.println("\n=== Creating SMS-In Export (HTTP IN and email trunks, yesterday) ===");
        Map<String, Object> inFilters = new HashMap<>();
        inFilters.put("from", from);
        inFilters.put("to", to);
        inFilters.put("trunk_type", Arrays.asList(
                EnumWire.value(ExportTrunkType.HTTP_IN), EnumWire.value(ExportTrunkType.EMAIL)));
        create(client, ExportType.SMS_IN, inFilters, "java-sms-in-" + suffix);

        System.out.println("\n=== Completed SMS-Out exports ===");
        QueryParams params = QueryParams.builder()
                .filter("export_type", EnumWire.value(ExportType.SMS_OUT))
                .filter("status", EnumWire.value(ExportStatus.COMPLETED))
                .build();
        List<Export> completed = client.exports().list(params).getData();
        System.out.println("Found " + completed.size() + " completed SMS-Out exports");
        if (completed.isEmpty() || completed.get(0).getUrl() == null) {
            return;
        }

        // The export file is a gzip-compressed CSV; download it and read its header line.
        Export export = completed.get(0);
        Path file = Files.createTempFile("didww-export-", ".csv.gz");
        try {
            client.downloadExport(export.getUrl(), file);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(Files.newInputStream(file)), StandardCharsets.UTF_8))) {
                System.out.println("Export " + export.getId() + " (" + export.getExportType() + ") columns: "
                        + reader.readLine());
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void create(DidwwClient client, ExportType type, Map<String, Object> filters,
                               String externalReferenceId) {
        Export export = new Export();
        export.setExportType(type);
        export.setFilters(filters);
        export.setExternalReferenceId(externalReferenceId);
        try {
            Export created = client.exports().create(export).getData();
            // The file URL is set once the export is completed: find the export again later to download it.
            System.out.println("Created " + type + " export: " + created.getId() + " (status: " + created.getStatus() + ")");
        } catch (DidwwApiException e) {
            System.out.println("Error creating " + type + " export: " + e.getMessage());
        }
    }
}
