package com.didww.sdk.internal;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Internal helper, not part of the SDK API: reads response bodies within a size
 * and a time limit.
 */
public final class Bodies {
    /** The most bytes of an error answer that are read. */
    public static final int MAX_ERROR_BODY_SIZE = 1 << 20;

    private static final long DEFAULT_DEADLINE_MILLIS = 60_000;

    private Bodies() {
    }

    /**
     * Returns the time allowed for reading a whole error body with {@code client}:
     * its read timeout, or 60 seconds when it has none.
     *
     * @param client the client that received the answer
     * @return the deadline in milliseconds
     */
    public static long errorBodyDeadlineMillis(OkHttpClient client) {
        int readTimeout = client.readTimeoutMillis();
        return readTimeout > 0 ? readTimeout : DEFAULT_DEADLINE_MILLIS;
    }

    /**
     * Reads at most {@code max} bytes of {@code body}, all of them within
     * {@code deadlineMillis} (0 = no deadline beyond the read timeout).
     *
     * @param body           the body to read, may be {@code null}
     * @param max            the most bytes to read
     * @param deadlineMillis the time allowed for the whole read
     * @return the bytes read
     * @throws IOException if the body cannot be read
     */
    public static byte[] readLimited(ResponseBody body, int max, long deadlineMillis) throws IOException {
        Buffer buffer = new Buffer();
        read(body, max, deadlineMillis, buffer);
        return buffer.readByteArray();
    }

    /**
     * Like {@link #readLimited(ResponseBody, int, long)}, but returns the bytes
     * read so far when the body breaks or the deadline passes: the status of an
     * error answer matters more than its body.
     *
     * @param body           the body to read, may be {@code null}
     * @param max            the most bytes to read
     * @param deadlineMillis the time allowed for the whole read
     * @return the bytes read
     */
    public static byte[] readLimitedQuietly(ResponseBody body, int max, long deadlineMillis) {
        Buffer buffer = new Buffer();
        try {
            read(body, max, deadlineMillis, buffer);
        } catch (IOException ignored) {
            // Keep what was read.
        }
        return buffer.readByteArray();
    }

    private static void read(ResponseBody body, int max, long deadlineMillis, Buffer buffer) throws IOException {
        if (body == null) {
            return;
        }
        BufferedSource source = body.source();
        if (deadlineMillis > 0) {
            source.timeout().deadline(deadlineMillis, TimeUnit.MILLISECONDS);
        }
        long remaining = max;
        while (remaining > 0) {
            long read = source.read(buffer, remaining);
            if (read == -1) {
                break;
            }
            remaining -= read;
        }
    }
}
