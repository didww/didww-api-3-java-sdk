package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class VoiceInTrunkTest extends BaseTest {

    @Test
    void testListVoiceInTrunks() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/voice_in_trunks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunks/index.json"))));

        ApiResponse<List<VoiceInTrunk>> response = client.voiceInTrunks().list();
        List<VoiceInTrunk> trunks = response.getData();

        assertThat(trunks).isNotEmpty();

        VoiceInTrunk first = trunks.get(0);
        assertThat(first.getId()).isEqualTo("9cbadd6f-0665-46bc-b3aa-687d22157808");
        assertThat(first.getName()).isEqualTo("iax2 trunk sample");
        assertThat(first.getPriority()).isEqualTo(1);
        assertThat(first.getWeight()).isEqualTo(65535);
        assertThat(first.getCliFormat()).isEqualTo("e164");
    }

    @Test
    void testCreateVoiceInTrunk() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/voice_in_trunks"))
                .withRequestBody(equalToJson(loadFixture("voice_in_trunks/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunks/create.json"))));

        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setName("hello, test pstn trunk");

        ApiResponse<VoiceInTrunk> response = client.voiceInTrunks().create(trunk);
        VoiceInTrunk created = response.getData();

        assertThat(created.getId()).isEqualTo("41b94706-325e-4704-a433-d65105758836");
        assertThat(created.getName()).isEqualTo("hello, test pstn trunk");
    }
}
