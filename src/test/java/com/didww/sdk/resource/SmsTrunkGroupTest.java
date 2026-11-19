package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.smstrunkconfiguration.HttpInConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmppSmscConfiguration;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsTrunkGroupTest extends BaseTest {

    private static final String GROUP_ID = "2b3c4d5e-0001-4b6c-9d7e-8f9a0b1c2d01";
    private static final String HTTP_IN_ID = "1a2b3c4d-0002-4a5b-8c6d-7e8f9a0b1c02";
    private static final String SMPP_SMSC_ID = "1a2b3c4d-0005-4a5b-8c6d-7e8f9a0b1c05";

    @Test
    void testListSmsTrunkGroups() {
        stubGetFixture("/v3/sms_trunk_groups", "sms_trunk_groups/index.json");

        List<SmsTrunkGroup> groups = client.smsTrunkGroups().list().getData();

        assertThat(groups).hasSize(2);
        SmsTrunkGroup first = groups.get(0);
        assertThat(first.getId()).isEqualTo(GROUP_ID);
        assertThat(first.getName()).isEqualTo("Inbound SMS");
        assertThat(first.getExternalReferenceId()).isNull();
        assertThat(first.getCreatedAt()).isEqualTo(OffsetDateTime.of(2026, 10, 1, 10, 0, 0, 0, ZoneOffset.UTC));
        assertThat(first.getUpdatedAt()).isEqualTo(OffsetDateTime.of(2026, 10, 1, 10, 0, 0, 0, ZoneOffset.UTC));
        assertThat(first.getMetaSmsTrunksCount()).isEqualTo(2);
        assertThat(groups.get(1).getName()).isEqualTo("Outbound SMS");
        assertThat(groups.get(1).getMetaSmsTrunksCount()).isEqualTo(0);
    }

    @Test
    void testFindSmsTrunkGroupWithIncludedSmsTrunks() {
        stubGetFixture("/v3/sms_trunk_groups/" + GROUP_ID, "sms_trunk_groups/show_with_sms_trunks.json");

        SmsTrunkGroup group = client.smsTrunkGroups().find(GROUP_ID,
                QueryParams.builder().include("sms_trunks").build()).getData();

        assertThat(group.getSmsTrunks()).hasSize(2);
        assertThat(group.getSmsTrunks().get(0).getId()).isEqualTo(HTTP_IN_ID);
        assertThat(group.getSmsTrunks().get(0).getConfiguration()).isInstanceOf(HttpInConfiguration.class);
        assertThat(group.getSmsTrunks().get(1).getId()).isEqualTo(SMPP_SMSC_ID);
        assertThat(group.getSmsTrunks().get(1).getConfiguration()).isInstanceOf(SmppSmscConfiguration.class);
        assertThat(group.getMetaSmsTrunksCount()).isEqualTo(2);
    }

    @Test
    void testFindSmsTrunkGroupNotFound() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/sms_trunk_groups/" + GROUP_ID))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunk_groups/not_found.json"))));

        assertThatThrownBy(() -> client.smsTrunkGroups().find(GROUP_ID))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> assertThat(((DidwwApiException) thrown).getHttpStatus()).isEqualTo(404));
    }

    @Test
    void testCreateSmsTrunkGroup() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/sms_trunk_groups"))
                .withRequestBody(equalToJson(loadFixture("sms_trunk_groups/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunk_groups/create.json"))));

        SmsTrunkGroup group = new SmsTrunkGroup();
        group.setName("Inbound SMS");
        group.setSmsTrunks(Collections.singletonList(new SmsTrunk().withId(HTTP_IN_ID)));

        SmsTrunkGroup created = client.smsTrunkGroups().create(group).getData();

        assertThat(created.getId()).isEqualTo(GROUP_ID);
        assertThat(created.getName()).isEqualTo("Inbound SMS");
        assertThat(created.getMetaSmsTrunksCount()).isEqualTo(1);
    }

    @Test
    void testUpdateExternalReferenceId() {
        stubPatch("sms_trunk_groups/update_external_reference_id_request.json", "sms_trunk_groups/update_external_reference_id.json");

        SmsTrunkGroup group = new SmsTrunkGroup().withId(GROUP_ID);
        group.setExternalReferenceId("updated-ext-ref-99");

        SmsTrunkGroup updated = client.smsTrunkGroups().update(group).getData();

        assertThat(updated.getExternalReferenceId()).isEqualTo("updated-ext-ref-99");
    }

    @Test
    void testUpdateSmsTrunks() {
        stubPatch("sms_trunk_groups/update_sms_trunks_request.json", "sms_trunk_groups/update_sms_trunks.json");

        SmsTrunkGroup group = new SmsTrunkGroup().withId(GROUP_ID);
        group.setSmsTrunks(Arrays.asList(new SmsTrunk().withId(HTTP_IN_ID), new SmsTrunk().withId(SMPP_SMSC_ID)));

        SmsTrunkGroup updated = client.smsTrunkGroups().update(group).getData();

        assertThat(updated.getMetaSmsTrunksCount()).isEqualTo(2);
    }

    @Test
    void testUpdateClearsSmsTrunks() {
        stubPatch("sms_trunk_groups/update_clear_sms_trunks_request.json", "sms_trunk_groups/update_sms_trunks.json");

        SmsTrunkGroup group = new SmsTrunkGroup().withId(GROUP_ID);
        group.setSmsTrunks(Collections.emptyList());

        SmsTrunkGroup updated = client.smsTrunkGroups().update(group).getData();

        assertThat(updated.getId()).isEqualTo(GROUP_ID);
    }

    @Test
    void testUpdateAfterIncludeDoesNotEchoIncomingDids() {
        stubGetFixture("/v3/sms_trunk_groups/" + GROUP_ID, "sms_trunk_groups/show_with_incoming_dids.json");
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sms_trunk_groups/" + GROUP_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunk_groups/update_external_reference_id.json"))));

        SmsTrunkGroup group = client.smsTrunkGroups().find(GROUP_ID,
                QueryParams.builder().include("incoming_dids").build()).getData();
        assertThat(group.getIncomingDids()).hasSize(1);
        group.setName("renamed");
        client.smsTrunkGroups().update(group);

        assertThat(lastPatchBody()).contains("\"name\":\"renamed\"").doesNotContain("incoming_dids");
    }

    @Test
    void testDeleteSmsTrunkGroup() {
        wireMock.stubFor(delete(urlPathEqualTo("/v3/sms_trunk_groups/" + GROUP_ID))
                .willReturn(aResponse().withStatus(204)));

        client.smsTrunkGroups().delete(GROUP_ID);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/sms_trunk_groups/" + GROUP_ID)));
    }

    @Test
    void testDeleteSmsTrunkGroupInUse() {
        wireMock.stubFor(delete(urlPathEqualTo("/v3/sms_trunk_groups/" + GROUP_ID))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunk_groups/delete_error.json"))));

        assertThatThrownBy(() -> client.smsTrunkGroups().delete(GROUP_ID))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> {
                    DidwwApiException ex = (DidwwApiException) thrown;
                    assertThat(ex.getHttpStatus()).isEqualTo(422);
                    assertThat(ex.getErrors().get(0).getDetail())
                            .isEqualTo("base - Cannot delete an SMS trunk group while DIDs are routed to it or to its trunks");
                });
    }

    @Test
    void testMetaSmsTrunksCountIsNullWhenAbsentOrInvalid() throws Exception {
        assertThat(new SmsTrunkGroup().getMetaSmsTrunksCount()).isNull();

        java.lang.reflect.Field meta = MetaResource.class.getDeclaredField("meta");
        meta.setAccessible(true);
        MetaMap map = new MetaMap();
        map.put("sms_trunks_count", "many");
        SmsTrunkGroup group = new SmsTrunkGroup();
        meta.set(group, map);

        assertThat(group.getMetaSmsTrunksCount()).isNull();
    }

    private void stubPatch(String requestFixture, String responseFixture) {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sms_trunk_groups/" + GROUP_ID))
                .withRequestBody(equalToJson(loadFixture(requestFixture), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture(responseFixture))));
    }

    private String lastPatchBody() {
        List<LoggedRequest> requests = wireMock.findAll(patchRequestedFor(urlPathEqualTo("/v3/sms_trunk_groups/" + GROUP_ID)));
        assertThat(requests).isNotEmpty();
        return requests.get(requests.size() - 1).getBodyAsString();
    }
}
