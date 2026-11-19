package com.didww.sdk.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * Internal helper, not part of the SDK API: copies a response body to a
 * destination, telling read failures from write failures.
 */
public final class Streams {
    private static final int BUFFER_SIZE = 64 * 1024;

    private Streams() {
    }

    /**
     * Copies {@code in} to {@code out}. A failure to read {@code in} is turned
     * into the exception made by {@code readFailure}; a failure to write
     * {@code out} is thrown as an {@link UncheckedIOException} with that failure
     * as its cause.
     *
     * @param in          the source
     * @param out         the destination
     * @param readFailure makes the exception thrown for a read failure
     * @return the number of bytes copied
     */
    public static long copy(InputStream in, OutputStream out, Function<IOException, RuntimeException> readFailure) {
        byte[] buffer = new byte[BUFFER_SIZE];
        long total = 0;
        while (true) {
            int read;
            try {
                read = in.read(buffer);
            } catch (IOException e) {
                throw readFailure.apply(e);
            }
            if (read < 0) {
                return total;
            }
            try {
                out.write(buffer, 0, read);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            total += read;
        }
    }
}
