package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Country;

import java.util.List;

public class CountriesExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // List all countries
        List<Country> countries = client.countries().list().getData();
        for (Country country : countries) {
            System.out.println(country.getName() + " (+" + country.getPrefix() + ") [" + country.getIso() + "]");
        }

        // Filter countries by name
        QueryParams params = QueryParams.builder()
                .filter("name", "United States")
                .page(1, 10)
                .build();
        List<Country> filtered = client.countries().list(params).getData();
        System.out.println("\nFiltered: " + filtered.size() + " countries");

        // Find a specific country
        if (!filtered.isEmpty()) {
            Country country = client.countries().find(filtered.get(0).getId()).getData();
            System.out.println("Found: " + country.getName());
        }
    }
}
