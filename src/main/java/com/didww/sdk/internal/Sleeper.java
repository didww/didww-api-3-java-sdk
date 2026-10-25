package com.didww.sdk.internal;

import java.time.Duration;

/**
 * Internal helper, not part of the SDK API: waits between attempts.
 */
@FunctionalInterface
public interface Sleeper {
    /** Sleeps the calling thread with {@link Thread#sleep(long, int)}. */
    Sleeper SYSTEM = duration -> {
        if (duration.isNegative() || duration.isZero()) {
            return;
        }
        long nanos = duration.toNanos();
        Thread.sleep(nanos / 1_000_000, (int) (nanos % 1_000_000));
    };

    /**
     * Waits for {@code duration}.
     *
     * @param duration the time to wait
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    void sleep(Duration duration) throws InterruptedException;
}
