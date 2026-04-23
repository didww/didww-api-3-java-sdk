package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DidHistoryTest extends BaseTest {

    @Test
    void testListDidHistory() {
        stubGetFixture("/v3/did_history", "did_history/index.json");

        ApiResponse<List<DidHistory>> response = client.didHistory().list();
        List<DidHistory> records = response.getData();

        assertThat(records).hasSize(2);

        DidHistory first = records.get(0);
        assertThat(first.getId()).isEqualTo("11111111-2222-3333-4444-555555555555");
        assertThat(first.getDidNumber()).isEqualTo("442038680521");
        assertThat(first.getAction()).isEqualTo("assigned");
        assertThat(first.getMethod()).isEqualTo("api3");
        assertThat(first.getCreatedAt()).isNotNull();

        DidHistory second = records.get(1);
        assertThat(second.getAction()).isEqualTo("renewed");
        assertThat(second.getMethod()).isEqualTo("system");
    }

    @Test
    void testFindDidHistory() {
        String id = "01234567-89ab-cdef-0123-456789abcdef";
        stubGetFixture("/v3/did_history/" + id, "did_history/show.json");

        ApiResponse<DidHistory> response = client.didHistory().find(id);
        DidHistory record = response.getData();

        assertThat(record.getId()).isEqualTo(id);
        assertThat(record.getDidNumber()).isEqualTo("442038680521");
        assertThat(record.getAction()).isEqualTo("renewed");
        assertThat(record.getMethod()).isEqualTo("system");
        assertThat(record.getCreatedAt()).isNotNull();
        assertThat(record.getMetaFrom()).isNull();
        assertThat(record.getMetaTo()).isNull();
    }

    @Test
    void testFindDidHistoryBillingCyclesCountChanged() {
        String id = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        stubGetFixture("/v3/did_history/" + id, "did_history/show_billing_cycles.json");

        ApiResponse<DidHistory> response = client.didHistory().find(id);
        DidHistory record = response.getData();

        assertThat(record.getId()).isEqualTo(id);
        assertThat(record.getDidNumber()).isEqualTo("442038680521");
        assertThat(record.getAction()).isEqualTo("billing_cycles_count_changed");
        assertThat(record.getMethod()).isEqualTo("api3");
        assertThat(record.getCreatedAt()).isNotNull();
        assertThat(record.getMetaFrom()).isEqualTo("2");
        assertThat(record.getMetaTo()).isEqualTo("1");
    }
}
