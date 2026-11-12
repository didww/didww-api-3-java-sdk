package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * RTCP feedback (AVPF) on the calls of a trunk: not used, accepted when the other side offers it,
 * or initiated by DIDWW. (API 2026-10-22)
 *
 * <p>A value this version of the SDK does not know is kept as read and sent back unchanged.
 */
public final class RtcpFeedbackMode extends StringValue {
    public static final RtcpFeedbackMode DISABLED = new RtcpFeedbackMode("disabled");
    public static final RtcpFeedbackMode ACCEPT = new RtcpFeedbackMode("accept");
    public static final RtcpFeedbackMode INITIATE = new RtcpFeedbackMode("initiate");

    private static final Map<String, RtcpFeedbackMode> KNOWN = byValue(DISABLED, ACCEPT, INITIATE);

    private RtcpFeedbackMode(String value) {
        super(value);
    }

    /**
     * @param value the name of the value in the API
     * @return the constant of a known value, or a new instance holding a value this version of the SDK
     *     does not know
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RtcpFeedbackMode of(String value) {
        RtcpFeedbackMode known = KNOWN.get(value);
        return known != null ? known : new RtcpFeedbackMode(value);
    }
}
