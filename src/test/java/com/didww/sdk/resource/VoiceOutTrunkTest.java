package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.authenticationmethod.CredentialsAndIpAuthenticationMethod;
import com.didww.sdk.resource.authenticationmethod.IpOnlyAuthenticationMethod;
import com.didww.sdk.resource.enums.DefaultDstAction;
import com.didww.sdk.resource.enums.MediaEncryptionMode;
import com.didww.sdk.resource.enums.OnCliMismatchAction;
import com.didww.sdk.resource.enums.VoiceOutTrunkStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class VoiceOutTrunkTest extends BaseTest {

    @Test
    void testListVoiceOutTrunks() {
        stubGetFixture("/v3/voice_out_trunks", "voice_out_trunks/index.json");

        ApiResponse<List<VoiceOutTrunk>> response = client.voiceOutTrunks().list();
        List<VoiceOutTrunk> trunks = response.getData();

        assertThat(trunks).hasSize(2);

        // First entry — credentials_and_ip with 2026-04-16 fields
        VoiceOutTrunk first = trunks.get(0);
        assertThat(first.getStatus()).isEqualTo(VoiceOutTrunkStatus.BLOCKED);
        assertThat(first.getEmergencyEnableAll()).isFalse();
        assertThat(first.getRtpTimeout()).isEqualTo(30);
        assertThat(first.getAuthenticationMethod()).isInstanceOf(CredentialsAndIpAuthenticationMethod.class);

        // Second entry — credentials_and_ip with 2026-04-16 fields
        VoiceOutTrunk second = trunks.get(1);
        assertThat(second.getStatus()).isEqualTo(VoiceOutTrunkStatus.ACTIVE);
        assertThat(second.getEmergencyEnableAll()).isFalse();
        assertThat(second.getRtpTimeout()).isNull();
        assertThat(second.getAuthenticationMethod()).isInstanceOf(CredentialsAndIpAuthenticationMethod.class);
        CredentialsAndIpAuthenticationMethod secondAuth = (CredentialsAndIpAuthenticationMethod) second.getAuthenticationMethod();
        assertThat(secondAuth.getAllowedSipIps()).containsExactly("203.0.113.0/24");
        assertThat(secondAuth.getUsername()).isEqualTo("50fb4hugfv");
    }

    @Test
    void testFindVoiceOutTrunk() {
        stubGetFixture("/v3/voice_out_trunks/425ce763-a3a9-49b4-af5b-ada1a65c8864", "voice_out_trunks/show.json");

        QueryParams params = QueryParams.builder()
                .include("dids", "default_did")
                .build();
        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().find("425ce763-a3a9-49b4-af5b-ada1a65c8864", params);
        VoiceOutTrunk trunk = response.getData();

        assertThat(trunk.getName()).isEqualTo("test");
        assertThat(trunk.getStatus()).isEqualTo(VoiceOutTrunkStatus.BLOCKED);
        assertThat(trunk.getCapacityLimit()).isEqualTo(123);
        assertThat(trunk.getAllowAnyDidAsCli()).isFalse();
        assertThat(trunk.getOnCliMismatchAction()).isEqualTo(OnCliMismatchAction.REPLACE_CLI);
        assertThat(trunk.getMediaEncryptionMode()).isEqualTo(MediaEncryptionMode.SRTP_SDES);
        assertThat(trunk.getDefaultDstAction()).isEqualTo(DefaultDstAction.REJECT_ALL);
        assertThat(trunk.getForceSymmetricRtp()).isTrue();
        assertThat(trunk.getRtpPing()).isTrue();
        assertThat(trunk.getThresholdReached()).isFalse();
        assertThat(trunk.getThresholdAmount()).isEqualTo("200.0");
        assertThat(trunk.getCallbackUrl()).isNull();
        assertThat(trunk.getDstPrefixes()).containsExactly("370");
        assertThat(trunk.getAllowedRtpIps()).isNull();
        assertThat(trunk.getDids()).hasSize(2);
        assertThat(trunk.getDefaultDid()).isNotNull();
        assertThat(trunk.getDefaultDid().getNumber()).isEqualTo("37061498222");

        // authentication_method
        assertThat(trunk.getAuthenticationMethod()).isInstanceOf(CredentialsAndIpAuthenticationMethod.class);
        CredentialsAndIpAuthenticationMethod auth = (CredentialsAndIpAuthenticationMethod) trunk.getAuthenticationMethod();
        assertThat(auth.getAllowedSipIps()).containsExactly("203.0.113.1/32");
        assertThat(auth.getUsername()).isEqualTo("dpjgwbbac9");
        assertThat(auth.getPassword()).isEqualTo("z0hshvbcy7");
    }

    @Test
    void testCreateVoiceOutTrunk() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/voice_out_trunks"))
                .withRequestBody(equalToJson(loadFixture("voice_out_trunks/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/create.json"))));

        Did did = new Did().withId("7a028c32-e6b6-4c86-bf01-90f901b37012");

        CredentialsAndIpAuthenticationMethod auth = new CredentialsAndIpAuthenticationMethod();
        auth.setAllowedSipIps(Collections.singletonList("203.0.113.0/24"));

        VoiceOutTrunk trunk = new VoiceOutTrunk();
        trunk.setName("java-test");
        trunk.setOnCliMismatchAction(OnCliMismatchAction.REPLACE_CLI);
        trunk.setAuthenticationMethod(auth);
        trunk.setDefaultDid(did);
        trunk.setDids(Collections.singletonList(did));

        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().create(trunk);
        VoiceOutTrunk created = response.getData();

        assertThat(created.getId()).isEqualTo("b60201c1-21f0-4d9a-aafa-0e6d1e12f22e");
        assertThat(created.getName()).isEqualTo("java-test");
        assertThat(created.getStatus()).isEqualTo(VoiceOutTrunkStatus.ACTIVE);
        assertThat(created.getAuthenticationMethod()).isInstanceOf(CredentialsAndIpAuthenticationMethod.class);
        CredentialsAndIpAuthenticationMethod createdAuth = (CredentialsAndIpAuthenticationMethod) created.getAuthenticationMethod();
        assertThat(createdAuth.getUsername()).isEqualTo("dLPa6JbLTeMjKjl5");
        assertThat(createdAuth.getPassword()).isEqualTo("BZj1YvP45yWvX5Ic");
    }

    @Test
    void testUpdateVoiceOutTrunk() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/voice_out_trunks/425ce763-a3a9-49b4-af5b-ada1a65c8864"))
                .withRequestBody(equalToJson(loadFixture("voice_out_trunks/update_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/update.json"))));

        IpOnlyAuthenticationMethod auth = new IpOnlyAuthenticationMethod();
        auth.setAllowedSipIps(Collections.singletonList("203.0.113.1/32"));

        VoiceOutTrunk trunk = new VoiceOutTrunk().withId("425ce763-a3a9-49b4-af5b-ada1a65c8864");
        trunk.setName("test");
        trunk.setCapacityLimit(123);
        trunk.setOnCliMismatchAction(OnCliMismatchAction.REPLACE_CLI);
        trunk.setDefaultDstAction(DefaultDstAction.REJECT_ALL);
        trunk.setDstPrefixes(Collections.singletonList("370"));
        trunk.setForceSymmetricRtp(true);
        trunk.setRtpPing(true);
        trunk.setAuthenticationMethod(auth);

        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().update(trunk);
        VoiceOutTrunk updated = response.getData();

        assertThat(updated.getId()).isEqualTo("425ce763-a3a9-49b4-af5b-ada1a65c8864");
        assertThat(updated.getName()).isEqualTo("test");
        assertThat(updated.getStatus()).isEqualTo(VoiceOutTrunkStatus.BLOCKED);
        assertThat(updated.getCapacityLimit()).isEqualTo(123);
        assertThat(updated.getOnCliMismatchAction()).isEqualTo(OnCliMismatchAction.REPLACE_CLI);
        assertThat(updated.getDefaultDstAction()).isEqualTo(DefaultDstAction.REJECT_ALL);
        assertThat(updated.getDstPrefixes()).containsExactly("370");
        assertThat(updated.getForceSymmetricRtp()).isTrue();
        assertThat(updated.getRtpPing()).isTrue();
    }

    @Test
    void testUpdateVoiceOutTrunkAuthenticationMethodOnly() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/voice_out_trunks/425ce763-a3a9-49b4-af5b-ada1a65c8864"))
                .withRequestBody(equalToJson(loadFixture("voice_out_trunks/update_auth_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/update.json"))));

        CredentialsAndIpAuthenticationMethod auth = new CredentialsAndIpAuthenticationMethod();
        auth.setAllowedSipIps(Collections.singletonList("192.0.2.10/32"));
        auth.setTechPrefix("99");

        VoiceOutTrunk trunk = new VoiceOutTrunk().withId("425ce763-a3a9-49b4-af5b-ada1a65c8864");
        trunk.setAuthenticationMethod(auth);

        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().update(trunk);
        VoiceOutTrunk updated = response.getData();

        assertThat(updated.getId()).isEqualTo("425ce763-a3a9-49b4-af5b-ada1a65c8864");
    }

    @Test
    void testUpdateVoiceOutTrunkEmergencyEnableAll() {
        String id = "01234567-89ab-cdef-0123-456789abcdef";
        wireMock.stubFor(patch(urlPathEqualTo("/v3/voice_out_trunks/" + id))
                .withRequestBody(equalToJson(loadFixture("voice_out_trunks/update_emergency_enable_all_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/update_emergency_dids.json"))));

        VoiceOutTrunk trunk = new VoiceOutTrunk().withId(id);
        trunk.setEmergencyEnableAll(true);

        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().update(trunk);

        assertThat(response.getData().getId()).isEqualTo(id);
    }

    @Test
    void testUpdateVoiceOutTrunkEmergencyDids() {
        String id = "01234567-89ab-cdef-0123-456789abcdef";
        wireMock.stubFor(patch(urlPathEqualTo("/v3/voice_out_trunks/" + id))
                .withRequestBody(equalToJson(loadFixture("voice_out_trunks/update_emergency_dids_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/update_emergency_dids.json"))));

        VoiceOutTrunk trunk = new VoiceOutTrunk().withId(id);
        trunk.setEmergencyDids(Arrays.asList(
                new Did().withId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                new Did().withId("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        ));

        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().update(trunk);

        assertThat(response.getData().getId()).isEqualTo(id);
    }

    @Test
    void testUpdateVoiceOutTrunkClearEmergencyDids() {
        String id = "01234567-89ab-cdef-0123-456789abcdef";
        wireMock.stubFor(patch(urlPathEqualTo("/v3/voice_out_trunks/" + id))
                .withRequestBody(equalToJson(loadFixture("voice_out_trunks/clear_emergency_dids_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/update_emergency_dids.json"))));

        VoiceOutTrunk trunk = new VoiceOutTrunk().withId(id);
        trunk.setEmergencyDids(Collections.emptyList());

        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().update(trunk);

        assertThat(response.getData().getId()).isEqualTo(id);
    }

    @Test
    void testDeleteVoiceOutTrunk() {
        String id = "b60201c1-21f0-4d9a-aafa-0e6d1e12f22e";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/voice_out_trunks/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.voiceOutTrunks().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/voice_out_trunks/" + id)));
    }
}
