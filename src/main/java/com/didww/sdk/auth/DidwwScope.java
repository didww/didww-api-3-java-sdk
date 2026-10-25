package com.didww.sdk.auth;

/**
 * The scope requested for access tokens.
 */
public enum DidwwScope {
    /** Read-only tokens: every method but GET and HEAD is refused. */
    READ("read"),
    /** Tokens allowing every method. */
    FULL("full");

    private final String value;

    DidwwScope(String value) {
        this.value = value;
    }

    /**
     * Returns the wire value of the scope.
     *
     * @return {@code read} or {@code full}
     */
    public String getValue() {
        return value;
    }
}
