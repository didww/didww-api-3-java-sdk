package com.didww.sdk.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Internal helper, not part of the SDK API: a {@link GZIPInputStream} that reads
 * every gzip member of a network stream (see {@link ProbingInputStream}) and
 * reads the source to its end before it reports the end of the data.
 * {@link GZIPInputStream} takes a read failure inside the header of a following
 * member for the end of the data; reading on raises that failure again. Data
 * after the last member is ignored.
 */
public final class MultiMemberGzipInputStream extends GZIPInputStream {
    private boolean drained;

    /**
     * Wraps {@code in} and reads the header of the first member; closing this
     * stream closes {@code in}.
     *
     * @param in   the gzip data
     * @param size the input buffer size
     * @throws IOException if the header cannot be read or is not a gzip header
     */
    public MultiMemberGzipInputStream(InputStream in, int size) throws IOException {
        super(new ProbingInputStream(in), size);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int read = super.read(buf, off, len);
        if (read < 0 && !drained) {
            byte[] skipped = new byte[8192];
            while (in.read(skipped) >= 0) {
                // Data after the last member.
            }
            drained = true;
        }
        return read;
    }
}
