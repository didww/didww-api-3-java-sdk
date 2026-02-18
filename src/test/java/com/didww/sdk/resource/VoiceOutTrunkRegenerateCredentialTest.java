package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class VoiceOutTrunkRegenerateCredentialTest extends BaseTest {

    @Test
    void testCreateVoiceOutTrunkRegenerateCredential() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/voice_out_trunk_regenerate_credentials"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_out_trunk_regenerate_credentials/create.json"))));

        VoiceOutTrunkRegenerateCredential credential = new VoiceOutTrunkRegenerateCredential();

        ApiResponse<VoiceOutTrunkRegenerateCredential> response =
                client.voiceOutTrunkRegenerateCredentials().create(credential);
        VoiceOutTrunkRegenerateCredential created = response.getData();

        assertThat(created.getId()).isEqualTo("5fc59e7e-79eb-498a-8779-800416b5c68a");
    }
}
