package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * RTP and RTCP multiplexed on one port on the calls of a trunk: not used, accepted when the other
 * side offers it, or initiated by DIDWW. (API 2026-10-22)
 *
 * <p>A value this version of the SDK does not know is kept as read and sent back unchanged.
 */
public final class RtcpMuxMode extends StringValue {
    public static final RtcpMuxMode DISABLED = new RtcpMuxMode("disabled");
    public static final RtcpMuxMode ACCEPT = new RtcpMuxMode("accept");
    public static final RtcpMuxMode INITIATE = new RtcpMuxMode("initiate");

    private static final Map<String, RtcpMuxMode> KNOWN = byValue(DISABLED, ACCEPT, INITIATE);

    private RtcpMuxMode(String value) {
        super(value);
    }

    /**
     * @param value the name of the value in the API
     * @return the constant of a known value, or a new instance holding a value this version of the SDK
     *     does not know
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RtcpMuxMode of(String value) {
        RtcpMuxMode known = KNOWN.get(value);
        return known != null ? known : new RtcpMuxMode(value);
    }
}
