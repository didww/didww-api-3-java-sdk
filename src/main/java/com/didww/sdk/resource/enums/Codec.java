package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * Codec of an inbound SIP trunk ({@code SipConfiguration#setCodecs}). A Voice OUT trunk has its own
 * list, {@link VoiceOutCodec}. (API 2026-10-22)
 *
 * <p>A value this version of the SDK does not know is kept as read and sent back unchanged.
 */
public final class Codec extends StringValue {
    public static final Codec TELEPHONE_EVENT = new Codec("telephone_event");
    public static final Codec G723 = new Codec("g723");
    public static final Codec G729 = new Codec("g729");
    public static final Codec PCMU = new Codec("pcmu");
    public static final Codec PCMA = new Codec("pcma");
    public static final Codec SPEEX = new Codec("speex");
    public static final Codec GSM = new Codec("gsm");
    public static final Codec G726_32 = new Codec("g726_32");
    public static final Codec G721 = new Codec("g721");
    public static final Codec G726_24 = new Codec("g726_24");
    public static final Codec G726_40 = new Codec("g726_40");
    public static final Codec G726_16 = new Codec("g726_16");
    public static final Codec L16 = new Codec("l16");
    public static final Codec AMR = new Codec("amr");
    public static final Codec AMR_WB = new Codec("amr_wb");

    private static final Map<String, Codec> KNOWN = byValue(TELEPHONE_EVENT, G723, G729, PCMU, PCMA, SPEEX, GSM, G726_32, G721, G726_24, G726_40, G726_16, L16, AMR, AMR_WB);

    private Codec(String value) {
        super(value);
    }

    /**
     * @param value the name of the value in the API
     * @return the constant of a known value, or a new instance holding a value this version of the SDK
     *     does not know
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Codec of(String value) {
        Codec known = KNOWN.get(value);
        return known != null ? known : new Codec(value);
    }
}
