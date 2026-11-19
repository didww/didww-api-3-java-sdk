package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.AvailableDid;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.DidGroup;
import com.didww.sdk.resource.Region;
import com.didww.sdk.resource.StockKeepingUnit;
import com.didww.sdk.resource.VoiceInTrunk;

import java.util.Collections;
import java.util.List;

/**
 * Reads resources together with their related resources in one request, using include:
 * regions with their country, DID groups with their SKUs, available DIDs with a nested include
 * (did_group.stock_keeping_units) and DIDs with their Voice IN trunk.
 *
 * How the SDK exposes a relationship:
 *   - not included: the API sends only the links of the relationship, so the getter returns null;
 *   - included: the getter returns the related resource with its attributes (a list for a to-many
 *     relationship), and an included relationship that has no value, such as the trunk of a DID that
 *     is not routed to one, is null as well.
 * So include every relationship you read.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.IncludesExample
 */
public class IncludesExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // --- Not included vs included ---
        System.out.println("=== Regions without include: country not loaded ===");
        List<Region> plain = client.regions().list(QueryParams.builder().page(1, 3).build()).getData();
        for (Region region : plain) {
            System.out.println("  " + region.getName() + " - getCountry() = " + region.getCountry());
        }

        System.out.println("\n=== Regions with include=country ===");
        QueryParams regionParams = QueryParams.builder()
                .include("country")
                .page(1, 3)
                .build();
        for (Region region : client.regions().list(regionParams).getData()) {
            String country = region.getCountry() != null
                    ? region.getCountry().getName() + " (" + region.getCountry().getIso() + ")"
                    : "null";
            System.out.println("  " + region.getName() + " - country: " + country);
        }

        // --- To-many relationship: DID groups with their stock keeping units ---
        System.out.println("\n=== DID groups with include=stock_keeping_units ===");
        QueryParams groupParams = QueryParams.builder()
                .include("stock_keeping_units")
                .page(1, 3)
                .build();
        for (DidGroup group : client.didGroups().list(groupParams).getData()) {
            List<StockKeepingUnit> skus = orEmpty(group.getStockKeepingUnits());
            System.out.println("  " + group.getPrefix() + " " + group.getAreaName() + " - " + skus.size() + " SKU(s)");
            for (StockKeepingUnit sku : skus) {
                System.out.println("    $" + sku.getMonthlyPrice() + "/mo, setup $" + sku.getSetupPrice()
                        + ", " + sku.getChannelsIncludedCount() + " channel(s)");
            }
        }

        // --- Nested include: available DIDs with their DID group and its SKUs ---
        System.out.println("\n=== Available DIDs with include=did_group.stock_keeping_units ===");
        QueryParams availableParams = QueryParams.builder()
                .include("did_group.stock_keeping_units")
                .page(1, 3)
                .build();
        for (AvailableDid available : client.availableDids().list(availableParams).getData()) {
            DidGroup group = available.getDidGroup();
            if (group == null) {
                System.out.println("  " + available.getNumber() + " - DID group not included");
                continue;
            }
            System.out.println("  " + available.getNumber() + " - group " + group.getPrefix() + " "
                    + group.getAreaName() + ", " + orEmpty(group.getStockKeepingUnits()).size() + " SKU(s)");
        }

        // --- DIDs with their Voice IN trunk ---
        System.out.println("\n=== DIDs with include=voice_in_trunk ===");
        QueryParams didParams = QueryParams.builder()
                .include("voice_in_trunk")
                .page(1, 3)
                .build();
        List<Did> dids = client.dids().list(didParams).getData();
        if (dids.isEmpty()) {
            System.out.println("  No DIDs found. Order a DID first.");
        }
        for (Did did : dids) {
            VoiceInTrunk trunk = did.getVoiceInTrunk();
            System.out.println("  " + did.getNumber() + " - trunk: " + (trunk != null ? trunk.getName() : "(none)"));
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}
