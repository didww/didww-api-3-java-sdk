package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Region;

import java.util.List;

public class RegionsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // Fetch regions filtered by country and name, with included country
        QueryParams params = QueryParams.builder()
                .filter("country.id", "1f6fc2bd-f081-4202-9b1a-d9cb88d942b9")
                .filter("name", "Arizona")
                .include("country")
                .sort("-name")
                .build();
        List<Region> regions = client.regions().list(params).getData();
        for (Region region : regions) {
            System.out.println(region.getId() + " - " + region.getName());
        }

        // Fetch a specific region
        if (!regions.isEmpty()) {
            QueryParams includeParams = QueryParams.builder()
                    .include("country")
                    .build();
            Region region = client.regions().find(regions.get(0).getId(), includeParams).getData();
            System.out.println("Found: " + region.getName());
        }
    }
}
