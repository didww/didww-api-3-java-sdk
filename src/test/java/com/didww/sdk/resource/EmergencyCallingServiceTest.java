package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.EmergencyCallingServiceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class EmergencyCallingServiceTest extends BaseTest {

    @Test
    void testListEmergencyCallingServices() {
        stubGetFixture("/v3/emergency_calling_services", "emergency_calling_services/index.json");

        ApiResponse<List<EmergencyCallingService>> response = client.emergencyCallingServices().list();
        List<EmergencyCallingService> records = response.getData();

        assertThat(records).hasSize(1);

        EmergencyCallingService first = records.get(0);
        assertThat(first.getName()).isEqualTo("London Office ECS");
        assertThat(first.getReference()).isEqualTo("ECS-0001");
        assertThat(first.getStatus()).isEqualTo(EmergencyCallingServiceStatus.ACTIVE);
        assertThat(first.isActive()).isTrue();
        assertThat(first.isCanceled()).isFalse();
        assertThat(first.isChangesRequired()).isFalse();
        assertThat(first.isInProcess()).isFalse();
        assertThat(first.isNewStatus()).isFalse();
        assertThat(first.isPendingUpdate()).isFalse();
        assertThat(first.getActivatedAt()).isNotNull();
        assertThat(first.getCanceledAt()).isNull();
        assertThat(first.getCreatedAt()).isNotNull();
        assertThat(first.getRenewDate()).isEqualTo(LocalDate.of(2026, 5, 1));
    }

    @Test
    void testFindEmergencyCallingService() {
        String id = "01234567-89ab-cdef-0123-456789abcdef";
        stubGetFixture("/v3/emergency_calling_services/" + id, "emergency_calling_services/show.json");

        ApiResponse<EmergencyCallingService> response = client.emergencyCallingServices().find(id);
        EmergencyCallingService record = response.getData();

        assertThat(record.getId()).isEqualTo(id);
        assertThat(record.getName()).isEqualTo("Berlin Office ECS");
        assertThat(record.getReference()).isEqualTo("ECS-0042");
        assertThat(record.getStatus()).isEqualTo(EmergencyCallingServiceStatus.PENDING_UPDATE);
        assertThat(record.isPendingUpdate()).isTrue();
        assertThat(record.isActive()).isFalse();
        assertThat(record.getActivatedAt()).isNotNull();
        assertThat(record.getRenewDate()).isEqualTo(LocalDate.of(2026, 6, 15));
    }

    @Test
    void testDeleteEmergencyCallingService() {
        String id = "01234567-89ab-cdef-0123-456789abcdef";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/emergency_calling_services/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.emergencyCallingServices().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/emergency_calling_services/" + id)));
    }
}
