package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.EmergencyRequirement;
import com.didww.sdk.resource.enums.NumberType;

import java.util.List;

/**
 * Lists emergency service requirements, then the ones for one country and number type.
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
                .include("country")
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
            System.out.println("  Number type: " + req.getNumberType());
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

        // Filter by country and number type (the number_type filter takes the wire form, e.g. "local")
        if (!requirements.isEmpty() && requirements.get(0).getCountry() != null) {
            EmergencyRequirement first = requirements.get(0);
            String countryId = first.getCountry().getId();
            String countryName = first.getCountry().getName();
            NumberType numberType = first.getNumberType();
            QueryParams.Builder filters = QueryParams.builder().filter("country.id", countryId);
            String label = countryName;
            if (numberType != null) {
                filters.filter("number_type", EnumWire.value(numberType));
                label += " / " + EnumWire.value(numberType);
            }
            System.out.println("\n=== Requirements for " + label + " ===");
            QueryParams countryParams = filters.build();
            List<EmergencyRequirement> perCountry = client.emergencyRequirements().list(countryParams).getData();
            System.out.println("Found " + perCountry.size() + " requirements");
        }
    }
}
