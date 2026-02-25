package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.Export;
import com.didww.sdk.resource.enums.ExportType;

import java.util.List;
import java.util.Map;

/**
 * Exports: create and list CDR exports.
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.ExportsExample
 */
public class ExportsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Create an export
        Export export = new Export();
        export.setExportType(ExportType.CDR_IN);
        export.setFilters(Map.of("year", 2025, "month", 1));

        Export created = client.exports().create(export).getData();
        System.out.println("Created export: " + created.getId());
        System.out.println("  type: " + created.getExportType());
        System.out.println("  status: " + created.getStatus());

        // List exports
        List<Export> exports = client.exports().list(null).getData();
        System.out.println("\nAll exports (" + exports.size() + "):");
        for (Export e : exports) {
            System.out.println("  " + e.getId() + " " + e.getExportType() + " [" + e.getStatus() + "]");
        }
    }
}
