package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.enums.SenderIdVerificationStatus;
import com.didww.sdk.resource.enums.SenderType;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SenderIdVerificationTest extends BaseTest {

    private static final String ID = "e5a4e2f1-3b26-4a5c-9a0a-2f7c1f3e8d10";
    private static final String HTTP_OUT_ID = "1a2b3c4d-0003-4a5b-8c6d-7e8f9a0b1c03";

    @Test
    void testListSenderIdVerifications() {
        stubGetFixture("/v3/sender_id_verifications", "sender_id_verifications/index.json");

        List<SenderIdVerification> records = client.senderIdVerifications().list().getData();

        assertThat(records).hasSize(2);
        SenderIdVerification active = records.get(0);
        assertThat(active.getId()).isEqualTo(ID);
        assertThat(active.getName()).isEqualTo("ACME alerts");
        assertThat(active.getStatus()).isEqualTo(SenderIdVerificationStatus.ACTIVE);
        assertThat(active.isActive()).isTrue();
        assertThat(active.getSenderType()).isEqualTo(SenderType.LONG_CODE);
        assertThat(active.getSenderIds()).containsExactly("12125550100", "12125550101");
        assertThat(active.getReference()).isEqualTo("SID-1234");
        assertThat(active.getRejectReasons()).isNull();
        assertThat(active.getRejectComment()).isNull();
        assertThat(active.getRenewDate()).isEqualTo("2026-11-22");
        assertThat(active.getCreatedAt()).isEqualTo(OffsetDateTime.of(2026, 10, 1, 10, 0, 0, 0, ZoneOffset.UTC));
        assertThat(active.getActivatedAt()).isEqualTo(OffsetDateTime.of(2026, 10, 3, 12, 30, 0, 0, ZoneOffset.UTC));
        assertThat(active.getClosedAt()).isNull();
        assertThat(active.getMetaSetupPrice()).isEqualTo("10.0");
        assertThat(active.getMetaMonthlyPrice()).isEqualTo("15.0");

        SenderIdVerification rejected = records.get(1);
        assertThat(rejected.getStatus()).isEqualTo(SenderIdVerificationStatus.CHANGES_REQUIRED);
        assertThat(rejected.isChangesRequired()).isTrue();
        assertThat(rejected.isActive()).isFalse();
        assertThat(rejected.getSenderType()).isEqualTo(SenderType.ALPHANUMERIC);
        assertThat(rejected.getRejectReasons()).containsExactly("Company documents are missing");
        assertThat(rejected.getRejectComment()).isEqualTo("Upload the certificate of incorporation.");
        assertThat(rejected.getRenewDate()).isNull();
        assertThat(rejected.getActivatedAt()).isNull();
    }

    @Test
    void testFindSenderIdVerificationWithSmsTrunk() {
        stubGetFixture("/v3/sender_id_verifications/" + ID, "sender_id_verifications/show_with_sms_trunk.json");

        SenderIdVerification record = client.senderIdVerifications().find(ID,
                QueryParams.builder().include("sms_trunk").build()).getData();

        assertThat(record.getSmsTrunk()).isNotNull();
        assertThat(record.getSmsTrunk().getId()).isEqualTo(HTTP_OUT_ID);
        assertThat(record.getSmsTrunk().getName()).isEqualTo("Outbound HTTP");
        assertThat(record.hasDirtyFields()).isFalse();
    }

    @Test
    void testUpdateName() {
        stubPatch("sender_id_verifications/update_name_request.json", "sender_id_verifications/update_name.json");

        SenderIdVerification record = new SenderIdVerification().withId(ID);
        record.setName("ACME notifications");

        SenderIdVerification updated = client.senderIdVerifications().update(record).getData();

        assertThat(updated.getName()).isEqualTo("ACME notifications");
    }

    @Test
    void testUpdateLoadedSendsOnlyName() {
        stubGetFixture("/v3/sender_id_verifications", "sender_id_verifications/index.json");
        stubPatch("sender_id_verifications/update_name_request.json", "sender_id_verifications/update_name.json");

        SenderIdVerification record = client.senderIdVerifications().list().getData().get(0);
        record.setName("ACME notifications");

        SenderIdVerification updated = client.senderIdVerifications().update(record).getData();

        assertThat(updated.getName()).isEqualTo("ACME notifications");
    }

    @Test
    void testAssignSmsTrunk() {
        stubPatch("sender_id_verifications/assign_sms_trunk_request.json", "sender_id_verifications/assign_sms_trunk.json");

        SenderIdVerification record = new SenderIdVerification().withId(ID);
        record.setSmsTrunk(new SmsTrunk().withId(HTTP_OUT_ID));

        SenderIdVerification updated = client.senderIdVerifications().update(record).getData();

        assertThat(updated.getSmsTrunk()).isNotNull();
        assertThat(updated.getSmsTrunk().getId()).isEqualTo(HTTP_OUT_ID);
    }

    @Test
    void testUnassignSmsTrunk() {
        stubPatch("sender_id_verifications/unassign_sms_trunk_request.json", "sender_id_verifications/unassign_sms_trunk.json");

        SenderIdVerification record = new SenderIdVerification().withId(ID);
        record.setSmsTrunk(null);

        SenderIdVerification updated = client.senderIdVerifications().update(record).getData();

        assertThat(updated.getSmsTrunk()).isNull();
    }

    @Test
    void testUpdateAfterIncludeDoesNotEchoReadOnlyRelationships() {
        stubGetFixture("/v3/sender_id_verifications/" + ID,
                "sender_id_verifications/show_with_read_only_relationships.json");
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sender_id_verifications/" + ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sender_id_verifications/update_name.json"))));

        SenderIdVerification record = client.senderIdVerifications().find(ID, QueryParams.builder()
                .include("countries", "dids", "order", "identity", "address").build()).getData();
        assertThat(record.getCountries()).hasSize(1);
        assertThat(record.getDids()).hasSize(1);
        assertThat(record.getOrder()).isNotNull();
        assertThat(record.getIdentity()).isNotNull();
        assertThat(record.getAddress()).isNotNull();
        record.setName("renamed");
        client.senderIdVerifications().update(record);

        assertThat(lastPatchBody()).contains("\"name\":\"renamed\"")
                .doesNotContain("countries", "dids", "order", "identity", "address");
    }

    @Test
    void testUpdateClosedVerificationFails() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sender_id_verifications/" + ID))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sender_id_verifications/update_closed_error.json"))));

        SenderIdVerification record = new SenderIdVerification().withId(ID);
        record.setName("renamed");

        assertThatThrownBy(() -> client.senderIdVerifications().update(record))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> {
                    DidwwApiException ex = (DidwwApiException) thrown;
                    assertThat(ex.getHttpStatus()).isEqualTo(422);
                    assertThat(ex.getErrors().get(0).getDetail())
                            .isEqualTo("Sender ID Verification is closed and can not be changed");
                });
    }

    @Test
    void testStatusPredicates() throws Exception {
        Field status = SenderIdVerification.class.getDeclaredField("status");
        status.setAccessible(true);
        for (SenderIdVerificationStatus value : SenderIdVerificationStatus.values()) {
            SenderIdVerification record = new SenderIdVerification();
            status.set(record, value);
            assertThat(record.isActive()).isEqualTo(value == SenderIdVerificationStatus.ACTIVE);
            assertThat(record.isChangesRequired()).isEqualTo(value == SenderIdVerificationStatus.CHANGES_REQUIRED);
            assertThat(record.isClosed()).isEqualTo(value == SenderIdVerificationStatus.CLOSED);
            assertThat(record.isClosingInProcess()).isEqualTo(value == SenderIdVerificationStatus.CLOSING_IN_PROCESS);
            assertThat(record.isInProcess()).isEqualTo(value == SenderIdVerificationStatus.IN_PROCESS);
            assertThat(record.isNewStatus()).isEqualTo(value == SenderIdVerificationStatus.NEW);
        }
        SenderIdVerification unknown = new SenderIdVerification();
        assertThat(unknown.isActive()).isFalse();
        assertThat(unknown.isNewStatus()).isFalse();
    }

    private void stubPatch(String requestFixture, String responseFixture) {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sender_id_verifications/" + ID))
                .withRequestBody(equalToJson(loadFixture(requestFixture), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture(responseFixture))));
    }

    private String lastPatchBody() {
        List<LoggedRequest> requests = wireMock.findAll(patchRequestedFor(urlPathEqualTo("/v3/sender_id_verifications/" + ID)));
        assertThat(requests).isNotEmpty();
        return requests.get(requests.size() - 1).getBodyAsString();
    }
}
