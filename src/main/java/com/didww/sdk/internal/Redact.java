package com.didww.sdk.internal;

/**
 * Shared helpers for masking sensitive credential values in default
 * {@code toString} / debugger / log output. The wire format is never
 * affected — only Stringer-style methods route through these helpers.
 */
public final class Redact {

    private Redact() {}

    /**
     * Returns {@code "[FILTERED]"} for any non-null input and {@code "null"}
     * otherwise. The {@code "null"} passthrough avoids leaking
     * "this field was set" information when the value is genuinely unset.
     */
    public static String mask(String value) {
        return value == null ? "null" : "[FILTERED]";
    }
}
