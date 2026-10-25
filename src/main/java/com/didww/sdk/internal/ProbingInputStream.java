package com.didww.sdk.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Internal helper, not part of the SDK API: a stream whose {@link #available()}
 * reads one byte ahead when the source reports none. {@link java.util.zip.GZIPInputStream}
 * looks for a gzip member that follows another one only while {@code available()}
 * is positive, which a network stream rarely reports.
 */
public final class ProbingInputStream extends FilterInputStream {

    /**
     * Wraps {@code in}; closing this stream closes it.
     *
     * @param in the source
     */
    public ProbingInputStream(InputStream in) {
        super(new PushbackInputStream(in, 1));
    }

    /**
     * Returns the bytes the source reports as available, or else 1 after
     * reading one byte ahead, or 0 at the end of the data.
     *
     * @return the bytes that can be read
     * @throws IOException if the source cannot be read
     */
    @Override
    public int available() throws IOException {
        int available = super.available();
        if (available > 0) {
            return available;
        }
        PushbackInputStream source = (PushbackInputStream) in;
        int b = source.read();
        if (b < 0) {
            return 0;
        }
        source.unread(b);
        return 1;
    }
}
