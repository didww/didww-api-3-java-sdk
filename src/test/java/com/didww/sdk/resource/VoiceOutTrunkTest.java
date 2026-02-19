package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.DefaultDstAction;
import com.didww.sdk.resource.enums.MediaEncryptionMode;
import com.didww.sdk.resource.enums.OnCliMismatchAction;
import com.didww.sdk.resource.enums.VoiceOutTrunkStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class VoiceOutTrunkTest extends BaseTest {

    @Test
    void testListVoiceOutTrunks() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/voice_out_trunks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/index.json"))));

        ApiResponse<List<VoiceOutTrunk>> response = client.voiceOutTrunks().list();
        List<VoiceOutTrunk> trunks = response.getData();

        assertThat(trunks).isNotEmpty();
    }

    @Test
    void testFindVoiceOutTrunk() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/voice_out_trunks/425ce763-a3a9-49b4-af5b-ada1a65c8864"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/show.json"))));

        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().find("425ce763-a3a9-49b4-af5b-ada1a65c8864");
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
        assertThat(trunk.getUsername()).isEqualTo("dpjgwbbac9");
    }

    @Test
    void testCreateVoiceOutTrunk() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/voice_out_trunks"))
                .withRequestBody(equalToJson(loadFixture("voice_out_trunks/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunks/create.json"))));

        Did did = new Did();
        did.setId("7a028c32-e6b6-4c86-bf01-90f901b37012");

        VoiceOutTrunk trunk = new VoiceOutTrunk();
        trunk.setName("php-test");
        trunk.setAllowedSipIps(Collections.singletonList("0.0.0.0/0"));
        trunk.setOnCliMismatchAction(OnCliMismatchAction.REPLACE_CLI);
        trunk.setDefaultDid(did);
        trunk.setDids(Collections.singletonList(did));

        ApiResponse<VoiceOutTrunk> response = client.voiceOutTrunks().create(trunk);
        VoiceOutTrunk created = response.getData();

        assertThat(created.getId()).isEqualTo("b60201c1-21f0-4d9a-aafa-0e6d1e12f22e");
        assertThat(created.getName()).isEqualTo("php-test");
        assertThat(created.getStatus()).isEqualTo(VoiceOutTrunkStatus.ACTIVE);
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
