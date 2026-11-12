package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * ICE (Interactive Connectivity Establishment) on the calls of a trunk: not used, accepted when the
 * other side offers it, or initiated by DIDWW. (API 2026-10-22)
 *
 * <p>A value this version of the SDK does not know is kept as read and sent back unchanged.
 */
public final class IceMode extends StringValue {
    public static final IceMode DISABLED = new IceMode("disabled");
    public static final IceMode ACCEPT = new IceMode("accept");
    public static final IceMode INITIATE = new IceMode("initiate");

    private static final Map<String, IceMode> KNOWN = byValue(DISABLED, ACCEPT, INITIATE);

    private IceMode(String value) {
        super(value);
    }

    /**
     * @param value the name of the value in the API
     * @return the constant of a known value, or a new instance holding a value this version of the SDK
     *     does not know
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static IceMode of(String value) {
        IceMode known = KNOWN.get(value);
        return known != null ? known : new IceMode(value);
    }
}
