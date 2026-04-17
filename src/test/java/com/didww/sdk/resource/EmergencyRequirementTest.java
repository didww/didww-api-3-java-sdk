package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmergencyRequirementTest extends BaseTest {

    @Test
    void testListEmergencyRequirements() {
        stubGetFixture("/v3/emergency_requirements", "emergency_requirements/index.json");

        ApiResponse<List<EmergencyRequirement>> response = client.emergencyRequirements().list();
        List<EmergencyRequirement> records = response.getData();

        assertThat(records).hasSize(1);

        EmergencyRequirement first = records.get(0);
        assertThat(first.getIdentityType()).isEqualTo("personal");
        assertThat(first.getAddressAreaLevel()).isEqualTo("city");
        assertThat(first.getAddressMandatoryFields()).containsExactly("street", "city", "postal_code");
        assertThat(first.getPersonalMandatoryFields()).containsExactly("first_name", "last_name");
        assertThat(first.getBusinessMandatoryFields()).containsExactly("company_name", "tax_number");
        assertThat(first.getEstimateSetupTime()).isEqualTo("7-14 days");
        assertThat(first.getRequirementRestrictionMessage()).isNull();
    }

    @Test
    void testFindEmergencyRequirement() {
        String id = "01234567-89ab-cdef-0123-456789abcdef";
        stubGetFixture("/v3/emergency_requirements/" + id, "emergency_requirements/show.json");

        ApiResponse<EmergencyRequirement> response = client.emergencyRequirements().find(id);
        EmergencyRequirement record = response.getData();

        assertThat(record.getId()).isEqualTo(id);
        assertThat(record.getIdentityType()).isEqualTo("business");
        assertThat(record.getAddressAreaLevel()).isEqualTo("region");
        assertThat(record.getEstimateSetupTime()).isEqualTo("7-14 days");
        assertThat(record.getRequirementRestrictionMessage()).isEqualTo("Additional compliance review is required for this country.");
        assertThat(record.getBusinessMandatoryFields()).containsExactly("company_name", "tax_number", "registration_number");
    }
}
