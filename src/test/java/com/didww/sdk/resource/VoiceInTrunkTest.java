package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.configuration.PstnConfiguration;
import com.didww.sdk.resource.enums.CliFormat;
import com.didww.sdk.resource.enums.DiversionInjectMode;
import com.didww.sdk.resource.enums.DiversionRelayPolicy;
import com.didww.sdk.resource.enums.NetworkProtocolPriority;
import com.didww.sdk.resource.enums.Codec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.didww.sdk.resource.enums.ReroutingDisconnectCode;
import com.didww.sdk.resource.enums.RxDtmfFormat;
import com.didww.sdk.resource.enums.SstRefreshMethod;
import com.didww.sdk.resource.enums.MediaEncryptionMode;
import com.didww.sdk.resource.enums.StirShakenMode;
import com.didww.sdk.resource.enums.TransportProtocol;
import com.didww.sdk.resource.enums.TxDtmfFormat;
import com.didww.sdk.resource.configuration.SipConfiguration;
import com.didww.sdk.resource.configuration.TrunkConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class VoiceInTrunkTest extends BaseTest {

    @Test
    void testListVoiceInTrunks() {
        stubGetFixture("/v3/voice_in_trunks", "voice_in_trunks/index.json");

        QueryParams params = QueryParams.builder()
                .include("trunk_group", "pop")
                .build();
        ApiResponse<List<VoiceInTrunk>> response = client.voiceInTrunks().list(params);
        List<VoiceInTrunk> trunks = response.getData();

        assertThat(trunks).isNotEmpty();

        VoiceInTrunk first = trunks.get(0);
        assertThat(first.getId()).isEqualTo("2b4b1fcf-fe6a-4de9-8a58-7df46820ba13");
        assertThat(first.getName()).isEqualTo("sample trunk pstn");
        assertThat(first.getPriority()).isEqualTo(1);
        assertThat(first.getWeight()).isEqualTo(65535);
        assertThat(first.getCliFormat()).isEqualTo(CliFormat.E164);
        assertThat(first.getVoiceInTrunkGroup()).isNull();
        assertThat(first.getPop()).isNull();

        VoiceInTrunk second = trunks.get(1);
        assertThat(second.getPop()).isNotNull();
        assertThat(second.getPop().getName()).isEqualTo("DE, FRA");
    }

    @Test
    void testListSipConfigurationAttributes() {
        stubGetFixture("/v3/voice_in_trunks", "voice_in_trunks/index.json");

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
        assertThat(config.getHost()).isEqualTo("203.0.113.78");
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
        assertThat(config.getReroutingDisconnectCodeIds()).isNull();
    }

    @Test
    void testCreateSipTrunkWithReroutingDisconnectCodes() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/voice_in_trunks"))
                .withRequestBody(equalToJson(loadFixture("voice_in_trunks/create_sip_with_rerouting_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunks/create_sip_with_rerouting.json"))));

        SipConfiguration sipConfig = new SipConfiguration();
        sipConfig.setUsername("username");
        sipConfig.setHost("203.0.113.110");
        sipConfig.setSstRefreshMethodId(SstRefreshMethod.INVITE);
        sipConfig.setPort(5060);
        sipConfig.setCodecIds(Arrays.asList(Codec.PCMU, Codec.PCMA, Codec.G729, Codec.G723, Codec.TELEPHONE_EVENT));
        sipConfig.setReroutingDisconnectCodeIds(Arrays.asList(
                ReroutingDisconnectCode.SIP_400_BAD_REQUEST,
                ReroutingDisconnectCode.SIP_402_PAYMENT_REQUIRED,
                ReroutingDisconnectCode.SIP_403_FORBIDDEN,
                ReroutingDisconnectCode.SIP_404_NOT_FOUND,
                ReroutingDisconnectCode.SIP_408_REQUEST_TIMEOUT,
                ReroutingDisconnectCode.SIP_409_CONFLICT,
                ReroutingDisconnectCode.SIP_410_GONE,
                ReroutingDisconnectCode.SIP_412_CONDITIONAL_REQUEST_FAILED,
                ReroutingDisconnectCode.SIP_413_REQUEST_ENTITY_TOO_LARGE,
                ReroutingDisconnectCode.SIP_414_REQUEST_URI_TOO_LONG,
                ReroutingDisconnectCode.SIP_415_UNSUPPORTED_MEDIA_TYPE,
                ReroutingDisconnectCode.SIP_416_UNSUPPORTED_URI_SCHEME,
                ReroutingDisconnectCode.SIP_417_UNKNOWN_RESOURCE_PRIORITY,
                ReroutingDisconnectCode.SIP_420_BAD_EXTENSION,
                ReroutingDisconnectCode.SIP_421_EXTENSION_REQUIRED,
                ReroutingDisconnectCode.SIP_422_SESSION_INTERVAL_TOO_SMALL,
                ReroutingDisconnectCode.SIP_423_INTERVAL_TOO_BRIEF,
                ReroutingDisconnectCode.SIP_424_BAD_LOCATION_INFORMATION,
                ReroutingDisconnectCode.SIP_428_USE_IDENTITY_HEADER,
                ReroutingDisconnectCode.SIP_429_PROVIDE_REFERRER_IDENTITY,
                ReroutingDisconnectCode.SIP_433_ANONYMITY_DISALLOWED,
                ReroutingDisconnectCode.SIP_436_BAD_IDENTITY_INFO,
                ReroutingDisconnectCode.SIP_437_UNSUPPORTED_CERTIFICATE,
                ReroutingDisconnectCode.SIP_438_INVALID_IDENTITY_HEADER,
                ReroutingDisconnectCode.SIP_480_TEMPORARILY_UNAVAILABLE,
                ReroutingDisconnectCode.SIP_482_LOOP_DETECTED,
                ReroutingDisconnectCode.SIP_483_TOO_MANY_HOPS,
                ReroutingDisconnectCode.SIP_484_ADDRESS_INCOMPLETE,
                ReroutingDisconnectCode.SIP_485_AMBIGUOUS,
                ReroutingDisconnectCode.SIP_486_BUSY_HERE,
                ReroutingDisconnectCode.SIP_487_REQUEST_TERMINATED,
                ReroutingDisconnectCode.SIP_488_NOT_ACCEPTABLE_HERE,
                ReroutingDisconnectCode.SIP_494_SECURITY_AGREEMENT_REQUIRED,
                ReroutingDisconnectCode.SIP_500_SERVER_INTERNAL_ERROR,
                ReroutingDisconnectCode.SIP_501_NOT_IMPLEMENTED,
                ReroutingDisconnectCode.SIP_502_BAD_GATEWAY,
                ReroutingDisconnectCode.SIP_504_SERVER_TIME_OUT,
                ReroutingDisconnectCode.SIP_505_VERSION_NOT_SUPPORTED,
                ReroutingDisconnectCode.SIP_513_MESSAGE_TOO_LARGE,
                ReroutingDisconnectCode.SIP_580_PRECONDITION_FAILURE,
                ReroutingDisconnectCode.SIP_600_BUSY_EVERYWHERE,
                ReroutingDisconnectCode.SIP_603_DECLINE,
                ReroutingDisconnectCode.SIP_604_DOES_NOT_EXIST_ANYWHERE,
                ReroutingDisconnectCode.SIP_606_NOT_ACCEPTABLE,
                ReroutingDisconnectCode.RINGING_TIMEOUT
        ));
        sipConfig.setMediaEncryptionMode(MediaEncryptionMode.ZRTP);
        sipConfig.setStirShakenMode(StirShakenMode.PAI);
        sipConfig.setAllowedRtpIps(Arrays.asList("203.0.113.1"));

        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setName("hello, test sip trunk");
        trunk.setConfiguration(sipConfig);

        ApiResponse<VoiceInTrunk> response = client.voiceInTrunks().create(trunk);
        VoiceInTrunk created = response.getData();
        assertThat(created.getConfiguration()).isInstanceOf(SipConfiguration.class);

        SipConfiguration config = (SipConfiguration) created.getConfiguration();
        List<ReroutingDisconnectCode> codes = config.getReroutingDisconnectCodeIds();
        assertThat(codes).hasSize(45);
        assertThat(codes.get(0)).isEqualTo(ReroutingDisconnectCode.SIP_400_BAD_REQUEST);
        assertThat(codes.get(codes.size() - 1)).isEqualTo(ReroutingDisconnectCode.RINGING_TIMEOUT);
        assertThat(codes).contains(ReroutingDisconnectCode.SIP_480_TEMPORARILY_UNAVAILABLE);
        assertThat(config.getDiversionRelayPolicy()).isEqualTo(DiversionRelayPolicy.SIP);
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
    void testUpdatePstnTrunk() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/voice_in_trunks/41b94706-325e-4704-a433-d65105758836"))
                .withRequestBody(equalToJson(loadFixture("voice_in_trunks/update_pstn_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunks/update_pstn.json"))));

        PstnConfiguration config = new PstnConfiguration();
        config.setDst("558540420025");

        VoiceInTrunk trunk = new VoiceInTrunk().withId("41b94706-325e-4704-a433-d65105758836");
        trunk.setName("hello, updated test pstn trunk");
        trunk.setConfiguration(config);

        ApiResponse<VoiceInTrunk> response = client.voiceInTrunks().update(trunk);
        VoiceInTrunk updated = response.getData();

        assertThat(updated.getId()).isEqualTo("41b94706-325e-4704-a433-d65105758836");
        assertThat(updated.getName()).isEqualTo("hello, updated test pstn trunk");
        assertThat(updated.getConfiguration()).isInstanceOf(PstnConfiguration.class);
        PstnConfiguration updatedConfig = (PstnConfiguration) updated.getConfiguration();
        assertThat(updatedConfig.getDst()).isEqualTo("558540420025");
    }

    @Test
    void testUpdateSipTrunk() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/voice_in_trunks/a80006b6-4183-4865-8b99-7ebbd359a762"))
                .withRequestBody(equalToJson(loadFixture("voice_in_trunks/update_sip_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunks/update_sip.json"))));

        SipConfiguration sipConfig = new SipConfiguration();
        sipConfig.setUsername("new-username");
        sipConfig.setHost("203.0.113.110");
        sipConfig.setPort(5060);
        sipConfig.setCodecIds(Arrays.asList(Codec.PCMU, Codec.PCMA, Codec.G729, Codec.G723, Codec.TELEPHONE_EVENT));
        sipConfig.setSstRefreshMethodId(SstRefreshMethod.INVITE);
        sipConfig.setMediaEncryptionMode(MediaEncryptionMode.ZRTP);
        sipConfig.setStirShakenMode(StirShakenMode.PAI);
        sipConfig.setAllowedRtpIps(Arrays.asList("203.0.113.1"));

        VoiceInTrunk trunk = new VoiceInTrunk().withId("a80006b6-4183-4865-8b99-7ebbd359a762");
        trunk.setName("hello, updated test sip trunk");
        trunk.setDescription("just a description");
        trunk.setConfiguration(sipConfig);

        ApiResponse<VoiceInTrunk> response = client.voiceInTrunks().update(trunk);
        VoiceInTrunk updated = response.getData();

        assertThat(updated.getId()).isEqualTo("a80006b6-4183-4865-8b99-7ebbd359a762");
        assertThat(updated.getName()).isEqualTo("hello, updated test sip trunk");
        assertThat(updated.getDescription()).isEqualTo("just a description");
        assertThat(updated.getConfiguration()).isInstanceOf(SipConfiguration.class);
        SipConfiguration updatedSipConfig = (SipConfiguration) updated.getConfiguration();
        assertThat(updatedSipConfig.getUsername()).isEqualTo("new-username");
        assertThat(updatedSipConfig.getMediaEncryptionMode()).isEqualTo(MediaEncryptionMode.ZRTP);
        assertThat(updatedSipConfig.getStirShakenMode()).isEqualTo(StirShakenMode.PAI);
        assertThat(updatedSipConfig.getAllowedRtpIps()).containsExactly("203.0.113.1");
        assertThat(updatedSipConfig.getReroutingDisconnectCodeIds()).hasSize(45);
    }

    @Test
    void testDeleteVoiceInTrunk() {
        String id = "41b94706-325e-4704-a433-d65105758836";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/voice_in_trunks/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.voiceInTrunks().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/voice_in_trunks/" + id)));
    }

    // V3.5 SIP-registration attributes (API 2026-04-16)
    @Test
    void testSipConfigurationDeserializesV35AttributesIncludingReadOnlyCredentials() throws Exception {
        String json = "{\"type\":\"sip_configurations\",\"username\":\"username\",\"host\":\"example.com\","
                + "\"enabled_sip_registration\":true,\"use_did_in_ruri\":true,\"cnam_lookup\":true,"
                + "\"diversion_inject_mode\":\"did_number\",\"network_protocol_priority\":\"prefer_ipv4\","
                + "\"incoming_auth_username\":\"sipreg-user-1\","
                + "\"incoming_auth_password\":\"s3cret-Pa55\"}";

        ObjectMapper mapper = new ObjectMapper();
        SipConfiguration config = mapper.readValue(json, SipConfiguration.class);

        assertThat(config.getEnabledSipRegistration()).isTrue();
        assertThat(config.getUseDidInRuri()).isTrue();
        assertThat(config.getCnamLookup()).isTrue();
        assertThat(config.getDiversionInjectMode()).isEqualTo(DiversionInjectMode.DID_NUMBER);
        assertThat(config.getNetworkProtocolPriority()).isEqualTo(NetworkProtocolPriority.PREFER_IPV4);
        assertThat(config.getIncomingAuthUsername()).isEqualTo("sipreg-user-1");
        assertThat(config.getIncomingAuthPassword()).isEqualTo("s3cret-Pa55");
    }

    @Test
    void testSipConfigurationStripsReadOnlyCredentialsOnSerialization() throws Exception {
        // Simulate a caller that loaded a configuration from the server
        // (with incoming_auth_* populated) and is about to write it back.
        // The server returns 400 Param not allowed if these credentials are
        // echoed in the request, so the SDK MUST omit them from serialized
        // output. Jackson's @JsonProperty(access = WRITE_ONLY) handles this.
        SipConfiguration config = new SipConfiguration();
        config.setHost("example.com");
        config.setEnabledSipRegistration(true);
        config.setUseDidInRuri(true);
        config.setCnamLookup(true);
        config.setDiversionInjectMode(DiversionInjectMode.DID_NUMBER);
        config.setNetworkProtocolPriority(NetworkProtocolPriority.PREFER_IPV4);
        config.setIncomingAuthUsername("sipreg-user-1");
        config.setIncomingAuthPassword("s3cret-Pa55");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(config);

        assertThat(json).contains("\"enabled_sip_registration\":true");
        assertThat(json).contains("\"use_did_in_ruri\":true");
        assertThat(json).contains("\"cnam_lookup\":true");
        assertThat(json).contains("\"diversion_inject_mode\":\"did_number\"");
        assertThat(json).contains("\"network_protocol_priority\":\"prefer_ipv4\"");
        assertThat(json).doesNotContain("incoming_auth_username");
        assertThat(json).doesNotContain("incoming_auth_password");
    }
}
