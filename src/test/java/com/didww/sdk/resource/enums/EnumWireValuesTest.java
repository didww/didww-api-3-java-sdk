package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnumWireValuesTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static List<String> wire(Enum<?>[] constants) {
        return MAPPER.convertValue(constants, new TypeReference<List<String>>() { });
    }

    @Test
    void testInboundSipCodecs() {
        assertThat(wire(Codec.values())).containsExactly("telephone_event", "g723", "g729", "pcmu", "pcma",
                "speex", "gsm", "g726_32", "g721", "g726_24", "g726_40", "g726_16", "l16", "amr", "amr_wb");
    }

    @Test
    void testVoiceOutCodecs() {
        assertThat(wire(VoiceOutCodec.values())).containsExactly("telephone_event", "g723", "g729", "pcmu", "pcma",
                "gsm", "g722", "opus", "amr", "amr_wb");
    }

    @Test
    void testModes() {
        assertThat(wire(IceMode.values())).containsExactly("disabled", "accept", "initiate");
        assertThat(wire(RtcpMuxMode.values())).containsExactly("disabled", "accept", "initiate");
        assertThat(wire(RtcpFeedbackMode.values())).containsExactly("disabled", "accept", "initiate");
    }

    @Test
    void testPop() {
        assertThat(wire(Pop.values())).containsExactly("nyc", "fra", "lac", "mia", "sg", "hk", "ams");
    }

    @Test
    void testExportFilters() {
        assertThat(wire(ExportTrunkType.values())).containsExactly("smpp_smsc", "smpp_esme", "sms_to_email", "http_in");
        assertThat(wire(ExportServiceType.values())).containsExactly("a2p", "p2p");
    }
}
