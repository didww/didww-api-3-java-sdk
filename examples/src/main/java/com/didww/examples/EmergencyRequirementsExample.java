package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.EmergencyRequirement;

import java.util.List;

/**
 * Lists emergency service requirements for a country/did_group_type (2026-04-16).
 *
 * Emergency requirements describe what address precision, identity type,
 * and supporting fields an end-customer must provide to enable 911/112
 * on a DID.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.EmergencyRequirementsExample
 */
public class EmergencyRequirementsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        System.out.println("=== Emergency Requirements ===");
        QueryParams params = QueryParams.builder()
                .include("country", "did_group_type")
                .build();
        List<EmergencyRequirement> requirements = client.emergencyRequirements().list(params).getData();
        System.out.println("Found " + requirements.size() + " emergency requirements");

        int limit = Math.min(5, requirements.size());
        for (int i = 0; i < limit; i++) {
            EmergencyRequirement req = requirements.get(i);
            System.out.println("\nRequirement: " + req.getId());
            if (req.getCountry() != null) {
                System.out.println("  Country: " + req.getCountry().getName());
            }
            if (req.getDidGroupType() != null) {
                System.out.println("  DID Group Type: " + req.getDidGroupType().getName());
            }
            System.out.println("  Identity type required: " + req.getIdentityType());
            System.out.println("  Address area level: " + req.getAddressAreaLevel());
            if (req.getAddressMandatoryFields() != null) {
                System.out.println("  Address mandatory fields: " + String.join(", ", req.getAddressMandatoryFields()));
            }
            System.out.println("  Estimated setup time (days): " + req.getEstimateSetupTime());
            if (req.getRequirementRestrictionMessage() != null) {
                System.out.println("  Restriction: " + req.getRequirementRestrictionMessage());
            }
        }

        // Filter by country
        if (!requirements.isEmpty() && requirements.get(0).getCountry() != null) {
            String countryId = requirements.get(0).getCountry().getId();
            String countryName = requirements.get(0).getCountry().getName();
            System.out.println("\n=== Requirements for country " + countryName + " ===");
            QueryParams countryParams = QueryParams.builder()
                    .filter("country.id", countryId)
                    .build();
            List<EmergencyRequirement> perCountry = client.emergencyRequirements().list(countryParams).getData();
            System.out.println("Found " + perCountry.size() + " requirements");
        }
    }
}
