package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnumWireValuesTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static List<String> wire(Object[] constants) {
        return MAPPER.convertValue(constants, new TypeReference<List<String>>() { });
    }

    private static Object[] constants(Class<? extends StringValue> type) {
        return Arrays.stream(type.getFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == type)
                .map(field -> {
                    try {
                        return field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .toArray();
    }

    @Test
    void testInboundSipCodecs() {
        assertThat(wire(constants(Codec.class))).containsExactlyInAnyOrder("telephone_event", "g723", "g729", "pcmu", "pcma",
                "speex", "gsm", "g726_32", "g721", "g726_24", "g726_40", "g726_16", "l16", "amr", "amr_wb");
    }

    @Test
    void testVoiceOutCodecs() {
        assertThat(wire(constants(VoiceOutCodec.class))).containsExactlyInAnyOrder("telephone_event", "g723", "g729", "pcmu", "pcma",
                "gsm", "g722", "opus", "amr", "amr_wb");
    }

    @Test
    void testModes() {
        assertThat(wire(constants(IceMode.class))).containsExactlyInAnyOrder("disabled", "accept", "initiate");
        assertThat(wire(constants(RtcpMuxMode.class))).containsExactlyInAnyOrder("disabled", "accept", "initiate");
        assertThat(wire(constants(RtcpFeedbackMode.class))).containsExactlyInAnyOrder("disabled", "accept", "initiate");
    }

    @ParameterizedTest
    @ValueSource(classes = {Codec.class, VoiceOutCodec.class, IceMode.class, RtcpMuxMode.class, RtcpFeedbackMode.class})
    void testEveryConstantReadsBackAsItself(Class<? extends StringValue> type) {
        for (Object constant : constants(type)) {
            assertThat(MAPPER.convertValue(((StringValue) constant).getValue(), type)).isSameAs(constant);
        }
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
