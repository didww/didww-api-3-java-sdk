package com.didww.sdk.internal;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.function.DoubleSupplier;

/**
 * Internal helper, not part of the SDK API: random delays between attempts.
 */
public final class Jitter {
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Numbers in [0, 1) from a {@link SecureRandom}. */
    public static final DoubleSupplier SECURE_RANDOM = RANDOM::nextDouble;

    private static final long SECOND_NANOS = 1_000_000_000L;

    private Jitter() {
    }

    /**
     * Returns {@code retryAfter} plus a random extra of up to a fifth of it, or
     * up to a second when that is more, so that clients told the same
     * Retry-After do not come back in step.
     *
     * @param retryAfter the time the server asked to wait
     * @param random     numbers in [0, 1]
     * @return the time to wait
     */
    public static Duration spread(Duration retryAfter, DoubleSupplier random) {
        long maxExtra = Math.max(SECOND_NANOS, retryAfter.toNanos() / 5);
        return retryAfter.plusNanos((long) (random.getAsDouble() * maxExtra));
    }

    /**
     * Returns a full-jitter backoff: a random part of {@code min(cap, base * 2^attempt)}.
     *
     * @param attempt the number of the failed attempt, from 1
     * @param base    the base delay
     * @param cap     the largest delay
     * @param random  numbers in [0, 1]
     * @return the time to wait
     */
    public static Duration backoff(int attempt, Duration base, Duration cap, DoubleSupplier random) {
        long limit = base.toNanos() << Math.min(Math.max(attempt, 0), 16);
        limit = Math.min(cap.toNanos(), limit);
        return Duration.ofNanos((long) (random.getAsDouble() * limit));
    }

    /**
     * Returns a random lowercase hex string of {@code bytes} random bytes.
     *
     * @param bytes the number of random bytes
     * @return two hex digits per byte
     */
    public static String randomHex(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return Hex.lower(buffer);
    }
}
