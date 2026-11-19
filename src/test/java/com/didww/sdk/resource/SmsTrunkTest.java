package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.converter.DidwwResourceConverter;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.SenderIdVerificationStatus;
import com.didww.sdk.resource.enums.SenderType;
import com.didww.sdk.resource.enums.SmsBodyType;
import com.didww.sdk.resource.enums.SmsHttpMethod;
import com.didww.sdk.resource.enums.SmsServerLocation;
import com.didww.sdk.resource.smstrunkconfiguration.GenericSmsTrunkConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.HttpInConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.HttpOutConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmppEsmeConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmppSmscConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmsToEmailConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmsTrunkConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsTrunkTest extends BaseTest {

    private static final String HTTP_IN_ID = "1a2b3c4d-0002-4a5b-8c6d-7e8f9a0b1c02";
    private static final String HTTP_OUT_ID = "1a2b3c4d-0003-4a5b-8c6d-7e8f9a0b1c03";
    private static final String UNKNOWN_ID = "1a2b3c4d-0006-4a5b-8c6d-7e8f9a0b1c06";
    private static final String GROUP_ID = "2b3c4d5e-0001-4b6c-9d7e-8f9a0b1c2d01";
    private static final ObjectMapper DIDWW_MAPPER = DidwwResourceConverter.createObjectMapper();

    @Test
    void testListSmsTrunks() {
        stubGetFixture("/v3/sms_trunks", "sms_trunks/index.json");

        List<SmsTrunk> trunks = client.smsTrunks().list().getData();

        assertThat(trunks).hasSize(5);
        assertThat(trunks).extracting(t -> t.getConfiguration().getType())
                .containsExactly("sms_to_email", "http_in", "http_out", "smpp_esme", "smpp_smsc");

        SmsTrunk email = trunks.get(0);
        assertThat(email.getId()).isEqualTo("1a2b3c4d-0001-4a5b-8c6d-7e8f9a0b1c01");
        assertThat(email.getName()).isEqualTo("sms_to_email trunk");
        assertThat(email.getExternalReferenceId()).isNull();
        assertThat(email.getBlocked()).isFalse();
        assertThat(email.getPriority()).isEqualTo(100);
        assertThat(email.getAllowAnyDidAsSource()).isFalse();
        assertThat(email.getAllowIn()).isTrue();
        assertThat(email.getAllowOut()).isFalse();
        assertThat(email.getCreatedAt()).isEqualTo(OffsetDateTime.of(2026, 10, 1, 10, 0, 0, 0, ZoneOffset.UTC));
        assertThat(email.getUpdatedAt()).isEqualTo(OffsetDateTime.of(2026, 10, 1, 10, 0, 0, 0, ZoneOffset.UTC));
        SmsToEmailConfiguration emailCfg = (SmsToEmailConfiguration) email.getConfiguration();
        assertThat(emailCfg.getSendTo()).isEqualTo("sms@example.com");
        assertThat(emailCfg.getSubject()).isEqualTo("Inbound SMS");
        assertThat(emailCfg.getMessage()).isEqualTo("You have a new message.");

        HttpInConfiguration httpIn = (HttpInConfiguration) trunks.get(1).getConfiguration();
        assertThat(httpIn.getUrl()).isEqualTo("https://example.com/sms");
        assertThat(httpIn.getHttpMethod()).isEqualTo(SmsHttpMethod.POST);
        assertThat(httpIn.getQueryParameters()).isNull();
        assertThat(httpIn.getHeaders()).containsExactly(Map.entry("X-Source", "didww"));
        assertThat(httpIn.getBodyType()).isEqualTo(SmsBodyType.JSON);
        assertThat(httpIn.getBody()).isEqualTo("{\"text\":\"{SMS_TEXT}\",\"from\":\"{SMS_SRC_ADDR}\"}");
        assertThat(httpIn.getFollowRedirect()).isFalse();
        assertThat(httpIn.getValidateHttpsCertificate()).isTrue();

        SmsTrunk out = trunks.get(2);
        assertThat(out.getPriority()).isNull();
        assertThat(out.getAllowOut()).isTrue();
        assertThat(out.getMetaHost()).isEqualTo("https://sms-out.example.com/outbound_messages");
        HttpOutConfiguration httpOut = (HttpOutConfiguration) out.getConfiguration();
        assertThat(httpOut.getCallbackUrl()).isEqualTo("https://example.com/sms/status");
        assertThat(httpOut.getAllowedIps()).containsExactly("203.0.113.10");
        assertThat(httpOut.getAuthName()).isEqualTo("generated-name");
        assertThat(httpOut.getAuthPassword()).isEqualTo("generated-password");

        SmppEsmeConfiguration esme = (SmppEsmeConfiguration) trunks.get(3).getConfiguration();
        assertThat(esme.getSystemType()).isNull();
        assertThat(esme.getAllowedIps()).containsExactly("203.0.113.10");
        assertThat(esme.getServerLocation()).isEqualTo(SmsServerLocation.NYC);
        assertThat(esme.getSystemId()).isEqualTo("esme1234");
        assertThat(esme.getPassword()).isEqualTo("gen12345");

        SmppSmscConfiguration smsc = (SmppSmscConfiguration) trunks.get(4).getConfiguration();
        assertThat(smsc.getHost()).isEqualTo("smsc.example.com");
        assertThat(smsc.getTxPort()).isEqualTo(2775);
        assertThat(smsc.getRxPort()).isEqualTo(2776);
        assertThat(smsc.getTransceiverMode()).isTrue();
        assertThat(smsc.getConnectionTimeout()).isEqualTo(30);
        assertThat(smsc.getUseSsl()).isFalse();
        assertThat(smsc.getSystemId()).isEqualTo("didww");
        assertThat(smsc.getSystemType()).isNull();
        assertThat(smsc.getPassword()).isEqualTo("secret");
        assertThat(smsc.getServerLocation()).isEqualTo(SmsServerLocation.NYC);

        assertThat(trunks).allSatisfy(t -> assertThat(t.hasDirtyFields()).isFalse());
    }

    @Test
    void testListSmsTrunksWithIncludedSmsTrunkGroup() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/sms_trunks"))
                .withQueryParam("filter[configuration.type]", equalTo("http_in"))
                .withQueryParam("include", equalTo("sms_trunk_group"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/index_with_sms_trunk_group.json"))));

        QueryParams params = QueryParams.builder()
                .filter("configuration.type", "http_in")
                .include("sms_trunk_group")
                .build();
        List<SmsTrunk> trunks = client.smsTrunks().list(params).getData();

        assertThat(trunks).hasSize(1);
        assertThat(trunks.get(0).getSmsTrunkGroup()).isNotNull();
        assertThat(trunks.get(0).getSmsTrunkGroup().getId()).isEqualTo(GROUP_ID);
        assertThat(trunks.get(0).getSmsTrunkGroup().getName()).isEqualTo("Inbound SMS");
    }

    @Test
    void testFindSmsTrunk() {
        stubGetFixture("/v3/sms_trunks/" + HTTP_OUT_ID, "sms_trunks/show.json");

        SmsTrunk trunk = client.smsTrunks().find(HTTP_OUT_ID).getData();

        assertThat(trunk.getId()).isEqualTo(HTTP_OUT_ID);
        assertThat(trunk.getName()).isEqualTo("http_out trunk");
        assertThat(trunk.getMetaHost()).isEqualTo("https://sms-out.example.com/outbound_messages");
        assertThat(trunk.getConfiguration()).isInstanceOf(HttpOutConfiguration.class);
        assertThat(((HttpOutConfiguration) trunk.getConfiguration()).getAuthPassword()).isEqualTo("generated-password");
    }

    @Test
    void testFindSmsTrunkWithIncludedSenderIdVerifications() {
        stubGetFixture("/v3/sms_trunks/" + HTTP_OUT_ID, "sms_trunks/show_with_sender_id_verifications.json");

        SmsTrunk trunk = client.smsTrunks().find(HTTP_OUT_ID,
                QueryParams.builder().include("sender_id_verifications").build()).getData();

        assertThat(trunk.getSenderIdVerifications()).hasSize(1);
        SenderIdVerification siv = trunk.getSenderIdVerifications().get(0);
        assertThat(siv.getId()).isEqualTo("e5a4e2f1-3b26-4a5c-9a0a-2f7c1f3e8d10");
        assertThat(siv.getName()).isEqualTo("ACME alerts");
        assertThat(siv.getStatus()).isEqualTo(SenderIdVerificationStatus.ACTIVE);
        assertThat(siv.getSenderType()).isEqualTo(SenderType.LONG_CODE);
    }

    @Test
    void testFindSmsTrunkNotFound() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/sms_trunks/" + HTTP_OUT_ID))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/not_found.json"))));

        assertThatThrownBy(() -> client.smsTrunks().find(HTTP_OUT_ID))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> {
                    DidwwApiException ex = (DidwwApiException) thrown;
                    assertThat(ex.getHttpStatus()).isEqualTo(404);
                    assertThat(ex.getErrors()).hasSize(1);
                    assertThat(ex.getErrors().get(0).getTitle()).isEqualTo("Record not found");
                });
    }

    @Test
    void testCreateSmsToEmailTrunk() {
        SmsToEmailConfiguration cfg = new SmsToEmailConfiguration();
        cfg.setSendTo("sms@example.com");
        cfg.setSubject("Inbound SMS");
        cfg.setMessage("You have a new message.");

        assertCreate("sms_to_email", cfg, SmsToEmailConfiguration.class);
    }

    @Test
    void testCreateHttpInTrunk() {
        HttpInConfiguration cfg = new HttpInConfiguration();
        cfg.setUrl("https://example.com/sms");
        cfg.setHttpMethod(SmsHttpMethod.POST);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-Source", "didww");
        cfg.setHeaders(headers);
        cfg.setBodyType(SmsBodyType.JSON);
        cfg.setBody("{\"text\":\"{SMS_TEXT}\",\"from\":\"{SMS_SRC_ADDR}\"}");
        cfg.setFollowRedirect(false);
        cfg.setValidateHttpsCertificate(true);

        assertCreate("http_in", cfg, HttpInConfiguration.class);
    }

    @Test
    void testCreateHttpOutTrunk() throws Exception {
        // Read-only credentials are never sent, even when present on the object.
        HttpOutConfiguration cfg = DIDWW_MAPPER.readValue(
                "{\"auth_name\":\"ignored-name\",\"auth_password\":\"ignored-password\"}", HttpOutConfiguration.class);
        cfg.setCallbackUrl("https://example.com/sms/status");
        cfg.setAllowedIps(Collections.singletonList("203.0.113.10"));
        assertThat(cfg.getAuthPassword()).isEqualTo("ignored-password");

        assertCreate("http_out", cfg, HttpOutConfiguration.class);
    }

    @Test
    void testCreateSmppEsmeTrunk() throws Exception {
        SmppEsmeConfiguration cfg = DIDWW_MAPPER.readValue(
                "{\"system_id\":\"ignored-id\",\"password\":\"ignored-password\"}", SmppEsmeConfiguration.class);
        cfg.setAllowedIps(Collections.singletonList("203.0.113.10"));
        cfg.setServerLocation(SmsServerLocation.NYC);
        assertThat(cfg.getSystemId()).isEqualTo("ignored-id");

        assertCreate("smpp_esme", cfg, SmppEsmeConfiguration.class);
    }

    @Test
    void testCreateSmppSmscTrunk() {
        SmppSmscConfiguration cfg = new SmppSmscConfiguration();
        cfg.setHost("smsc.example.com");
        cfg.setTxPort(2775);
        cfg.setRxPort(2776);
        cfg.setTransceiverMode(true);
        cfg.setConnectionTimeout(30);
        cfg.setUseSsl(false);
        cfg.setSystemId("didww");
        cfg.setPassword("secret");
        cfg.setServerLocation(SmsServerLocation.NYC);

        assertCreate("smpp_smsc", cfg, SmppSmscConfiguration.class);
    }

    @Test
    void testCreateSmsTrunkWithRelationships() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/sms_trunks"))
                .withRequestBody(equalToJson(loadFixture("sms_trunks/create_with_relationships_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/create_smpp_smsc.json"))));

        SmppSmscConfiguration cfg = new SmppSmscConfiguration();
        cfg.setHost("smsc.example.com");

        SmsTrunk trunk = new SmsTrunk();
        trunk.setName("smpp_smsc trunk");
        trunk.setConfiguration(cfg);
        trunk.setSmsTrunkGroup(new SmsTrunkGroup().withId(GROUP_ID));
        trunk.setOutgoingDids(Collections.singletonList(new Did().withId("9df99644-f1a5-4a3c-99a4-559d758eb96b")));

        SmsTrunk created = client.smsTrunks().create(trunk).getData();

        assertThat(created.getId()).isNotNull();
    }

    @Test
    void testCreateSmsTrunkError() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/sms_trunks"))
                .withRequestBody(equalToJson(loadFixture("sms_trunks/create_error_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/create_error.json"))));

        SmsTrunk trunk = new SmsTrunk();
        trunk.setName("sms_to_email trunk");
        trunk.setConfiguration(new SmsToEmailConfiguration());

        assertThatThrownBy(() -> client.smsTrunks().create(trunk))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> {
                    DidwwApiException ex = (DidwwApiException) thrown;
                    assertThat(ex.getHttpStatus()).isEqualTo(422);
                    assertThat(ex.getErrors()).hasSize(1);
                    assertThat(ex.getErrors().get(0).getDetail()).isEqualTo("configuration - send_to can't be blank");
                    assertThat(ex.getErrors().get(0).getSource()).containsEntry("pointer", "/data/attributes/configuration");
                });
    }

    @Test
    void testUpdateNameAndConfiguration() {
        stubPatch(HTTP_IN_ID, "sms_trunks/update_http_in_request.json", "sms_trunks/update_http_in.json");

        HttpInConfiguration cfg = new HttpInConfiguration();
        cfg.setUrl("https://example.com/sms/v2");

        SmsTrunk trunk = new SmsTrunk().withId(HTTP_IN_ID);
        trunk.setName("Webhook");
        trunk.setConfiguration(cfg);

        SmsTrunk updated = client.smsTrunks().update(trunk).getData();

        assertThat(updated.getName()).isEqualTo("Webhook");
        HttpInConfiguration updatedCfg = (HttpInConfiguration) updated.getConfiguration();
        assertThat(updatedCfg.getUrl()).isEqualTo("https://example.com/sms/v2");
        assertThat(updatedCfg.getHttpMethod()).isEqualTo(SmsHttpMethod.POST);
    }

    @Test
    void testUpdateFromLoadedDoesNotSendCredentials() {
        stubGetFixture("/v3/sms_trunks/" + HTTP_OUT_ID, "sms_trunks/show.json");
        stubPatch(HTTP_OUT_ID, "sms_trunks/update_http_out_request.json", "sms_trunks/update_http_out.json");

        SmsTrunk trunk = client.smsTrunks().find(HTTP_OUT_ID).getData();
        HttpOutConfiguration cfg = (HttpOutConfiguration) trunk.getConfiguration();
        cfg.setCallbackUrl("https://example.com/sms/status/v2");
        trunk.setConfiguration(cfg);

        SmsTrunk updated = client.smsTrunks().update(trunk).getData();

        HttpOutConfiguration updatedCfg = (HttpOutConfiguration) updated.getConfiguration();
        assertThat(updatedCfg.getCallbackUrl()).isEqualTo("https://example.com/sms/status/v2");
        assertThat(updatedCfg.getAuthPassword()).isEqualTo("generated-password");
    }

    @Test
    void testUpdateFromLoadedWithoutChangesSendsNothing() throws Exception {
        stubGetFixture("/v3/sms_trunks/" + HTTP_OUT_ID, "sms_trunks/show.json");
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sms_trunks/" + HTTP_OUT_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/show.json"))));

        SmsTrunk trunk = client.smsTrunks().find(HTTP_OUT_ID).getData();
        client.smsTrunks().update(trunk);

        JsonNode data = new ObjectMapper().readTree(lastPatchBody(HTTP_OUT_ID)).get("data");
        assertThat(data.get("id").asText()).isEqualTo(HTTP_OUT_ID);
        assertThat(data.get("attributes")).isEmpty();
        assertThat(data.path("relationships")).isEmpty();
        assertThat(data.has("meta")).isFalse();
    }

    @Test
    void testUpdateClearsRelationships() {
        stubPatch(HTTP_IN_ID, "sms_trunks/update_clear_relationships_request.json", "sms_trunks/update_http_in.json");

        SmsTrunk trunk = new SmsTrunk().withId(HTTP_IN_ID);
        trunk.setOutgoingDids(Collections.emptyList());
        trunk.setSmsTrunkGroup(null);

        SmsTrunk updated = client.smsTrunks().update(trunk).getData();

        assertThat(updated.getId()).isEqualTo(HTTP_IN_ID);
    }

    @Test
    void testUpdateAfterIncludeDoesNotEchoReadOnlyRelationships() {
        stubGetFixture("/v3/sms_trunks/" + HTTP_OUT_ID, "sms_trunks/show_with_sender_id_verifications.json");
        stubGetFixture("/v3/sms_trunks/" + HTTP_IN_ID, "sms_trunks/show_with_incoming_dids.json");
        wireMock.stubFor(patch(urlPathMatching("/v3/sms_trunks/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/update_http_out.json"))));

        SmsTrunk withSivs = client.smsTrunks().find(HTTP_OUT_ID,
                QueryParams.builder().include("sender_id_verifications").build()).getData();
        assertThat(withSivs.getSenderIdVerifications()).hasSize(1);
        withSivs.setName("renamed");
        client.smsTrunks().update(withSivs);

        SmsTrunk withDids = client.smsTrunks().find(HTTP_IN_ID,
                QueryParams.builder().include("incoming_dids", "sms_trunk_group").build()).getData();
        assertThat(withDids.getIncomingDids()).hasSize(1);
        withDids.setName("renamed");
        client.smsTrunks().update(withDids);

        assertThat(lastPatchBody(HTTP_OUT_ID)).contains("\"name\":\"renamed\"").doesNotContain("sender_id_verifications");
        assertThat(lastPatchBody(HTTP_IN_ID)).contains("\"name\":\"renamed\"").doesNotContain("incoming_dids");
    }

    @Test
    void testAllowAnyDidAsSourceClearsIncludedOutgoingDids() throws Exception {
        stubGetFixture("/v3/sms_trunks/" + HTTP_OUT_ID, "sms_trunks/show_with_outgoing_dids.json");
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sms_trunks/" + HTTP_OUT_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/show.json"))));

        SmsTrunk trunk = client.smsTrunks().find(HTTP_OUT_ID,
                QueryParams.builder().include("outgoing_dids").build()).getData();
        assertThat(trunk.getOutgoingDids()).hasSize(1);
        trunk.setAllowAnyDidAsSource(true);
        client.smsTrunks().update(trunk);

        JsonNode data = new ObjectMapper().readTree(lastPatchBody(HTTP_OUT_ID)).get("data");
        assertThat(data.get("attributes").get("allow_any_did_as_source").asBoolean()).isTrue();
        assertThat(data.get("relationships").get("outgoing_dids").toString()).isEqualTo("{\"data\":[]}");
    }

    @Test
    void testLoadedAllowAnyDidAsSourceSendsNothingOnUpdate() throws Exception {
        stubGetFixture("/v3/sms_trunks/" + HTTP_OUT_ID, "sms_trunks/show_allow_any_did_as_source.json");
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sms_trunks/" + HTTP_OUT_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/show_allow_any_did_as_source.json"))));

        SmsTrunk trunk = client.smsTrunks().find(HTTP_OUT_ID).getData();
        assertThat(trunk.getAllowAnyDidAsSource()).isTrue();
        assertThat(trunk.getOutgoingDids()).isNull();
        trunk.setName("renamed");
        client.smsTrunks().update(trunk);

        JsonNode data = new ObjectMapper().readTree(lastPatchBody(HTTP_OUT_ID)).get("data");
        assertThat(data.get("attributes").toString()).isEqualTo("{\"name\":\"renamed\"}");
        assertThat(data.path("relationships")).isEmpty();
    }

    @Test
    void testUnknownConfigurationIsPreservedWithExactNumbers() {
        stubGetFixture("/v3/sms_trunks/" + UNKNOWN_ID, "sms_trunks/show_unknown_configuration.json");
        stubPatch(UNKNOWN_ID, "sms_trunks/update_unknown_configuration_request.json",
                "sms_trunks/show_unknown_configuration.json");

        SmsTrunk trunk = client.smsTrunks().find(UNKNOWN_ID).getData();

        assertThat(trunk.getConfiguration()).isInstanceOf(GenericSmsTrunkConfiguration.class);
        GenericSmsTrunkConfiguration generic = (GenericSmsTrunkConfiguration) trunk.getConfiguration();
        assertThat(generic.getType()).isEqualTo("future_transport");
        assertThat(generic.getAttributes().get("endpoint").asText()).isEqualTo("https://example.com/future");
        assertThat(generic.getAttributes().get("retries").asInt()).isEqualTo(3);
        assertThat(generic.getAttributes().get("enabled").asBoolean()).isTrue();
        assertThat(generic.getAttributes().get("ratio").decimalValue().toPlainString()).isEqualTo("1.10");

        generic.getAttributes().put("endpoint", "https://example.com/future/v2");
        trunk.setConfiguration(generic);
        client.smsTrunks().update(trunk);

        assertThat(lastPatchBody(UNKNOWN_ID))
                .contains("\"ratio\":1.10")
                .contains("\"sequence\":9007199254740993")
                .contains("[123456789012345678901234567890]")
                .contains("\"cleared\":null");
    }

    @Test
    void testUpdateGenericSendsNulls() {
        stubPatch(HTTP_IN_ID, "sms_trunks/update_generic_nulls_request.json", "sms_trunks/update_http_in.json");

        GenericSmsTrunkConfiguration cfg = new GenericSmsTrunkConfiguration("http_in");
        cfg.getAttributes().put("http_method", "get");
        cfg.getAttributes().putNull("body_type");
        cfg.getAttributes().putNull("body");

        SmsTrunk trunk = new SmsTrunk().withId(HTTP_IN_ID);
        trunk.setConfiguration(cfg);

        client.smsTrunks().update(trunk);

        assertThat(lastPatchBody(HTTP_IN_ID)).contains("\"body_type\":null").contains("\"body\":null");
    }

    @Test
    void testDeleteSmsTrunk() {
        wireMock.stubFor(delete(urlPathEqualTo("/v3/sms_trunks/" + HTTP_IN_ID))
                .willReturn(aResponse().withStatus(204)));

        client.smsTrunks().delete(HTTP_IN_ID);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/sms_trunks/" + HTTP_IN_ID)));
    }

    @Test
    void testDeleteSmsTrunkInUse() {
        wireMock.stubFor(delete(urlPathEqualTo("/v3/sms_trunks/" + HTTP_IN_ID))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/delete_error.json"))));

        assertThatThrownBy(() -> client.smsTrunks().delete(HTTP_IN_ID))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> {
                    DidwwApiException ex = (DidwwApiException) thrown;
                    assertThat(ex.getHttpStatus()).isEqualTo(422);
                    assertThat(ex.getErrors().get(0).getDetail())
                            .isEqualTo("base - Cannot delete an SMS trunk while DIDs are routed to it");
                });
    }

    private <C extends SmsTrunkConfiguration> void assertCreate(String type, C cfg, Class<C> expectedClass) {
        wireMock.stubFor(post(urlPathEqualTo("/v3/sms_trunks"))
                .withRequestBody(equalToJson(loadFixture("sms_trunks/create_" + type + "_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunks/create_" + type + ".json"))));

        SmsTrunk trunk = new SmsTrunk();
        trunk.setName(type + " trunk");
        trunk.setConfiguration(cfg);

        ApiResponse<SmsTrunk> response = client.smsTrunks().create(trunk);
        SmsTrunk created = response.getData();

        assertThat(created.getId()).isNotEmpty();
        assertThat(created.getName()).isEqualTo(type + " trunk");
        assertThat(created.getConfiguration()).isInstanceOf(expectedClass);
        assertThat(created.getConfiguration().getType()).isEqualTo(type);
    }

    private void stubPatch(String id, String requestFixture, String responseFixture) {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/sms_trunks/" + id))
                .withRequestBody(equalToJson(loadFixture(requestFixture), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture(responseFixture))));
    }

    private String lastPatchBody(String id) {
        List<LoggedRequest> requests = wireMock.findAll(patchRequestedFor(urlPathEqualTo("/v3/sms_trunks/" + id)));
        assertThat(requests).isNotEmpty();
        return requests.get(requests.size() - 1).getBodyAsString();
    }
}
