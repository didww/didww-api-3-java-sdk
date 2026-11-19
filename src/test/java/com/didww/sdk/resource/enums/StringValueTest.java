package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class StringValueTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testKnownValueReadsAsItsConstant() throws Exception {
        assertThat(MAPPER.readValue("\"pcmu\"", Codec.class)).isSameAs(Codec.PCMU);
        assertThat(MAPPER.readValue("\"opus\"", VoiceOutCodec.class)).isSameAs(VoiceOutCodec.OPUS);
        assertThat(MAPPER.readValue("\"accept\"", IceMode.class)).isSameAs(IceMode.ACCEPT);
        assertThat(MAPPER.readValue("\"accept\"", RtcpMuxMode.class)).isSameAs(RtcpMuxMode.ACCEPT);
        assertThat(MAPPER.readValue("\"accept\"", RtcpFeedbackMode.class)).isSameAs(RtcpFeedbackMode.ACCEPT);
    }

    @Test
    void testValueTheSdkDoesNotKnowIsKept() throws Exception {
        Codec codec = MAPPER.readValue("\"evs\"", Codec.class);

        assertThat(codec.getValue()).isEqualTo("evs");
        assertThat(codec).isEqualTo(Codec.of("evs")).hasSameHashCodeAs(Codec.of("evs"));
        assertThat(MAPPER.writeValueAsString(codec)).isEqualTo("\"evs\"");
        assertThat(MAPPER.readValue("\"require\"", IceMode.class).getValue()).isEqualTo("require");
    }

    @Test
    void testValuesOfDifferentTypesAreNotEqual() {
        assertThat(IceMode.ACCEPT).isNotEqualTo(RtcpMuxMode.ACCEPT);
        assertThat(Codec.PCMU).isNotEqualTo(VoiceOutCodec.PCMU);
    }

    @Test
    void testNullIsReadAndWrittenAsNull() throws Exception {
        List<Codec> codecs = MAPPER.readValue("[null,\"pcmu\"]", new TypeReference<List<Codec>>() { });

        assertThat(codecs).containsExactly(null, Codec.PCMU);
        assertThat(MAPPER.writeValueAsString(codecs)).isEqualTo("[null,\"pcmu\"]");
        assertThat(MAPPER.readValue("null", IceMode.class)).isNull();
    }

    @Test
    void testNullValueIsRejected() {
        assertThatNullPointerException().isThrownBy(() -> Codec.of(null));
    }

    @Test
    void testToStringIsTheValue() {
        assertThat(Arrays.asList(Codec.AMR_WB, Codec.of("evs"))).hasToString("[amr_wb, evs]");
    }
}
