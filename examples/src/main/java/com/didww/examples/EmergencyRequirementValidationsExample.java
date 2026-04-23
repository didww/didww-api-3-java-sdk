package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Address;
import com.didww.sdk.resource.EmergencyRequirement;
import com.didww.sdk.resource.EmergencyRequirementValidation;
import com.didww.sdk.resource.Identity;

import java.util.List;

/**
 * Validates an address+identity against an emergency requirement (2026-04-16).
 *
 * A successful POST returns 201 Created with the validation resource, meaning
 * the address and identity satisfy the emergency requirement. A validation
 * error (422) means they do not.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.EmergencyRequirementValidationsExample
 */
public class EmergencyRequirementValidationsExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // First, find an emergency requirement to validate against
        System.out.println("=== Finding an Emergency Requirement ===");
        QueryParams params = QueryParams.builder()
                .include("country", "did_group_type")
                .build();
        List<EmergencyRequirement> requirements = client.emergencyRequirements().list(params).getData();

        if (requirements.isEmpty()) {
            System.out.println("No emergency requirements found. Exiting.");
            return;
        }

        EmergencyRequirement requirement = requirements.get(0);
        System.out.println("Using requirement: " + requirement.getId());
        if (requirement.getCountry() != null) {
            System.out.println("  Country: " + requirement.getCountry().getName());
        }
        System.out.println("  Setup price:   " + requirement.getMetaSetupPrice());
        System.out.println("  Monthly price: " + requirement.getMetaMonthlyPrice());

        // To validate, you need an existing address and identity ID.
        // Replace these with real IDs from your account:
        String addressId = "YOUR_ADDRESS_ID";
        String identityId = "YOUR_IDENTITY_ID";

        System.out.println("\n=== Validating Emergency Requirement ===");
        System.out.println("  Requirement: " + requirement.getId());
        System.out.println("  Address: " + addressId);
        System.out.println("  Identity: " + identityId);

        EmergencyRequirementValidation validation = new EmergencyRequirementValidation();
        validation.setEmergencyRequirement(new EmergencyRequirement().withId(requirement.getId()));
        validation.setAddress(new Address().withId(addressId));
        validation.setIdentity(new Identity().withId(identityId));

        try {
            client.emergencyRequirementValidations().create(validation);
            System.out.println("Validation passed (201 Created)");
        } catch (Exception e) {
            System.out.println("Validation failed: " + e.getMessage());
        }
    }
}
