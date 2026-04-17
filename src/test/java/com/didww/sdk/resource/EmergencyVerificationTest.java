package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class EmergencyVerificationTest extends BaseTest {

    @Test
    void testListEmergencyVerifications() {
        stubGetFixture("/v3/emergency_verifications", "emergency_verifications/index.json");

        ApiResponse<List<EmergencyVerification>> response = client.emergencyVerifications().list();
        List<EmergencyVerification> records = response.getData();

        assertThat(records).hasSize(1);

        EmergencyVerification first = records.get(0);
        assertThat(first.getReference()).isEqualTo("EV-0001");
        assertThat(first.getStatus()).isEqualTo("pending");
        assertThat(first.getRejectReasons()).isNull();
        assertThat(first.getRejectComment()).isNull();
        assertThat(first.getCallbackUrl()).isEqualTo("https://example.com/emergency/hook");
        assertThat(first.getCallbackMethod()).isEqualTo("POST");
        assertThat(first.getExternalReferenceId()).isNull();
        assertThat(first.getCreatedAt()).isNotNull();
    }

    @Test
    void testFindEmergencyVerification() {
        String id = "01234567-89ab-cdef-0123-456789abcdef";
        stubGetFixture("/v3/emergency_verifications/" + id, "emergency_verifications/show.json");

        ApiResponse<EmergencyVerification> response = client.emergencyVerifications().find(id);
        EmergencyVerification record = response.getData();

        assertThat(record.getId()).isEqualTo(id);
        assertThat(record.getReference()).isEqualTo("EV-0042");
        assertThat(record.getStatus()).isEqualTo("rejected");
        assertThat(record.getRejectReasons()).containsExactly("Address does not match identity", "Missing proof of occupancy");
        assertThat(record.getRejectComment()).isEqualTo("Please re-submit with updated documentation.");
        assertThat(record.getExternalReferenceId()).isEqualTo("ref-xyz-999");
    }

    @Test
    void testCreateEmergencyVerification() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/emergency_verifications"))
                .withRequestBody(equalToJson(loadFixture("emergency_verifications/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("emergency_verifications/create.json"))));

        EmergencyCallingService ecs = new EmergencyCallingService().withId("33333333-4444-5555-6666-777777777777");
        Address address = new Address().withId("88888888-9999-aaaa-bbbb-cccccccccccc");
        Did did = new Did().withId("11111111-aaaa-bbbb-cccc-dddddddddddd");

        EmergencyVerification verification = new EmergencyVerification();
        verification.setCallbackUrl("https://example.com/emergency/hook");
        verification.setCallbackMethod("POST");
        verification.setExternalReferenceId("ref-abc-123");
        verification.setEmergencyCallingService(ecs);
        verification.setAddress(address);
        verification.setDids(Collections.singletonList(did));

        ApiResponse<EmergencyVerification> response = client.emergencyVerifications().create(verification);
        EmergencyVerification created = response.getData();

        assertThat(created.getId()).isEqualTo("bbbbbbbb-cccc-dddd-eeee-ffffffffffff");
        assertThat(created.getStatus()).isEqualTo("pending");
        assertThat(created.getReference()).isEqualTo("EV-0099");
        assertThat(created.getExternalReferenceId()).isEqualTo("ref-abc-123");
    }
}
