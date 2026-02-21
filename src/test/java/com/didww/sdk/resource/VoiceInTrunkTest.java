package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.configuration.PstnConfiguration;
import com.didww.sdk.resource.enums.CliFormat;
import com.didww.sdk.resource.enums.Codec;
import com.didww.sdk.resource.enums.RxDtmfFormat;
import com.didww.sdk.resource.enums.SstRefreshMethod;
import com.didww.sdk.resource.enums.MediaEncryptionMode;
import com.didww.sdk.resource.enums.StirShakenMode;
import com.didww.sdk.resource.enums.TransportProtocol;
import com.didww.sdk.resource.enums.TxDtmfFormat;
import com.didww.sdk.resource.configuration.SipConfiguration;
import com.didww.sdk.resource.configuration.TrunkConfiguration;
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
        assertThat(first.getCliFormat()).isEqualTo(CliFormat.E164);
    }

    @Test
    void testListSipConfigurationAttributes() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/voice_in_trunks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunks/index.json"))));

        ApiResponse<List<VoiceInTrunk>> response = client.voiceInTrunks().list();
        List<VoiceInTrunk> trunks = response.getData();

        // Find the SIP trunk in the list
        VoiceInTrunk sipTrunk = null;
        for (VoiceInTrunk t : trunks) {
            if (t.getConfiguration() instanceof SipConfiguration) {
                sipTrunk = t;
                break;
            }
        }
        assertThat(sipTrunk).isNotNull();
        SipConfiguration config = (SipConfiguration) sipTrunk.getConfiguration();
        assertThat(config.getUsername()).isEqualTo("username");
        assertThat(config.getHost()).isEqualTo("216.58.215.78");
        assertThat(config.getPort()).isEqualTo(8060);
        assertThat(config.getCodecIds()).containsExactly(Codec.PCMU, Codec.PCMA, Codec.G729);
        assertThat(config.getTransportProtocolId()).isEqualTo(TransportProtocol.UDP);
        assertThat(config.getAuthEnabled()).isTrue();
        assertThat(config.getAuthUser()).isEqualTo("auth_user");
        assertThat(config.getAuthPassword()).isEqualTo("auth_password");
        assertThat(config.getResolveRuri()).isTrue();
        assertThat(config.getRxDtmfFormatId()).isEqualTo(RxDtmfFormat.RFC_2833);
        assertThat(config.getTxDtmfFormatId()).isEqualTo(TxDtmfFormat.DISABLED);
        assertThat(config.getSstEnabled()).isFalse();
        assertThat(config.getSstMinTimer()).isEqualTo(600);
        assertThat(config.getSstMaxTimer()).isEqualTo(900);
        assertThat(config.getSstAccept501()).isTrue();
        assertThat(config.getSstRefreshMethodId()).isEqualTo(SstRefreshMethod.INVITE);
        assertThat(config.getSipTimerB()).isEqualTo(8000);
        assertThat(config.getDnsSrvFailoverTimer()).isEqualTo(2000);
        assertThat(config.getRtpPing()).isFalse();
        assertThat(config.getForceSymmetricRtp()).isFalse();
        assertThat(config.getMaxTransfers()).isEqualTo(2);
        assertThat(config.getMax30xRedirects()).isEqualTo(5);
        assertThat(config.getMediaEncryptionMode()).isEqualTo(MediaEncryptionMode.DISABLED);
        assertThat(config.getStirShakenMode()).isEqualTo(StirShakenMode.DISABLED);
        assertThat(config.getAllowedRtpIps()).isNull();
    }

    @Test
    void testCreateVoiceInTrunk() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/voice_in_trunks"))
                .withRequestBody(equalToJson(loadFixture("voice_in_trunks/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunks/create.json"))));

        PstnConfiguration config = new PstnConfiguration();
        config.setDst("558540420024");

        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setName("hello, test pstn trunk");
        trunk.setConfiguration(config);

        ApiResponse<VoiceInTrunk> response = client.voiceInTrunks().create(trunk);
        VoiceInTrunk created = response.getData();

        assertThat(created.getId()).isEqualTo("41b94706-325e-4704-a433-d65105758836");
        assertThat(created.getName()).isEqualTo("hello, test pstn trunk");
    }

    @Test
    void testDeleteVoiceInTrunk() {
        String id = "41b94706-325e-4704-a433-d65105758836";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/voice_in_trunks/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.voiceInTrunks().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/voice_in_trunks/" + id)));
    }
}
