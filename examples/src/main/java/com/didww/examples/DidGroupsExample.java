package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.DidGroup;

import java.util.List;

public class DidGroupsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Fetch DID groups filtered by country with included stock_keeping_units
        QueryParams params = QueryParams.builder()
                .filter("country.id", "1f6fc2bd-f081-4202-9b1a-d9cb88d942b9")
                .filter("area_name", "Beverly Hills")
                .include("stock_keeping_units")
                .build();
        List<DidGroup> didGroups = client.didGroups().list(params).getData();
        System.out.println("Found " + didGroups.size() + " DID groups");

        for (DidGroup didGroup : didGroups) {
            System.out.println(didGroup.getId()
                    + " - " + didGroup.getAreaName()
                    + " prefix=" + didGroup.getPrefix()
                    + " features=" + didGroup.getFeatures()
                    + " metered=" + didGroup.getIsMetered());
        }

        // Fetch a specific DID group
        if (!didGroups.isEmpty()) {
            QueryParams includeParams = QueryParams.builder()
                    .include("stock_keeping_units")
                    .build();
            DidGroup didGroup = client.didGroups().find(didGroups.get(0).getId(), includeParams).getData();
            System.out.println("Found: " + didGroup.getAreaName() + " prefix=" + didGroup.getPrefix());
        }
    }
}
