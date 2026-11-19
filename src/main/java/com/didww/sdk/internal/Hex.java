package com.didww.sdk.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Internal helper, not part of the SDK API: lowercase hex and SHA-256 digests.
 */
public final class Hex {
    private static final char[] DIGITS = "0123456789abcdef".toCharArray();

    private Hex() {
    }

    /**
     * Returns the lowercase hex form of {@code bytes}.
     *
     * @param bytes the bytes to encode
     * @return two hex digits per byte
     */
    public static String lower(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            out[i * 2] = DIGITS[(bytes[i] >> 4) & 0x0f];
            out[i * 2 + 1] = DIGITS[bytes[i] & 0x0f];
        }
        return new String(out);
    }

    /**
     * Returns the lowercase hex SHA-256 digest of the UTF-8 bytes of {@code value}.
     *
     * @param value the text to digest
     * @return 64 hex digits
     */
    public static String sha256(String value) {
        try {
            return lower(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
