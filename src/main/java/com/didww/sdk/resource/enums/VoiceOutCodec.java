package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * Codec of a Voice OUT trunk ({@code VoiceOutTrunk#setCodecs}). An inbound SIP trunk has its own
 * list, {@link Codec}. (API 2026-10-22)
 *
 * <p>A value this version of the SDK does not know is kept as read and sent back unchanged.
 */
public final class VoiceOutCodec extends StringValue {
    public static final VoiceOutCodec TELEPHONE_EVENT = new VoiceOutCodec("telephone_event");
    public static final VoiceOutCodec G723 = new VoiceOutCodec("g723");
    public static final VoiceOutCodec G729 = new VoiceOutCodec("g729");
    public static final VoiceOutCodec PCMU = new VoiceOutCodec("pcmu");
    public static final VoiceOutCodec PCMA = new VoiceOutCodec("pcma");
    public static final VoiceOutCodec GSM = new VoiceOutCodec("gsm");
    public static final VoiceOutCodec G722 = new VoiceOutCodec("g722");
    public static final VoiceOutCodec OPUS = new VoiceOutCodec("opus");
    public static final VoiceOutCodec AMR = new VoiceOutCodec("amr");
    public static final VoiceOutCodec AMR_WB = new VoiceOutCodec("amr_wb");

    private static final Map<String, VoiceOutCodec> KNOWN = byValue(TELEPHONE_EVENT, G723, G729, PCMU, PCMA, GSM, G722, OPUS, AMR, AMR_WB);

    private VoiceOutCodec(String value) {
        super(value);
    }

    /**
     * @param value the name of the value in the API
     * @return the constant of a known value, or a new instance holding a value this version of the SDK
     *     does not know
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VoiceOutCodec of(String value) {
        VoiceOutCodec known = KNOWN.get(value);
        return known != null ? known : new VoiceOutCodec(value);
    }
}
