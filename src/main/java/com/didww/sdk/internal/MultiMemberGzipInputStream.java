package com.didww.sdk.internal;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 * Internal helper, not part of the SDK API: a {@link GZIPInputStream} that reads
 * every gzip member of a network stream (see {@link ProbingInputStream}) and
 * reads the source to its end before it reports the end of the data.
 * {@link GZIPInputStream} takes a read failure inside the header of a following
 * member, and a following member that is cut short or corrupt in its header,
 * for the end of the data; this stream raises the failure instead. Other data
 * after the last member is ignored.
 */
public final class MultiMemberGzipInputStream extends GZIPInputStream {
    private static final int TRAILER_SIZE = 8;
    // A 10-byte header, the 2 bytes of empty deflate data and the trailer.
    private static final int MIN_MEMBER_SIZE = 20;

    private final RecordingInputStream source;
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
        this(new RecordingInputStream(new ProbingInputStream(in)), size);
    }

    private MultiMemberGzipInputStream(RecordingInputStream source, int size) throws IOException {
        super(source, size);
        this.source = source;
    }

    @Override
    protected void fill() throws IOException {
        super.fill();
        source.restart();
    }

    @Override
    public int read(byte[] b, int off, int length) throws IOException {
        int read = super.read(b, off, length);
        if (read < 0 && !drained) {
            byte[] skipped = new byte[8192];
            while (in.read(skipped) >= 0) {
                // Data after the last member.
            }
            checkDataAfterLastMember();
            drained = true;
        }
        return read;
    }

    // The data after the last trailer is what the inflater left in buf past the
    // trailer, followed by the source bytes read since buf was last filled
    // (after the trailer bytes among them).
    private void checkDataAfterLastMember() throws IOException {
        int remaining = inf.getRemaining();
        int buffered = Math.max(0, remaining - TRAILER_SIZE);
        int skip = Math.max(0, TRAILER_SIZE - remaining);
        long size = buffered + Math.max(0, source.count() - skip);
        int first = size > 0 ? byteAfterLastMember(0, buffered, skip) : -1;
        int second = size > 1 ? byteAfterLastMember(1, buffered, skip) : -1;
        boolean member = first == 0x1f && (size == 1 || second == 0x8b);
        if (!member) {
            return;
        }
        if (size < MIN_MEMBER_SIZE) {
            throw new EOFException("Unexpected end of gzip member");
        }
        throw new ZipException("Corrupt gzip member header");
    }

    private int byteAfterLastMember(int i, int buffered, int skip) {
        return i < buffered ? buf[len - buffered + i] & 0xff : source.recorded(skip + i - buffered);
    }

    // Records the first bytes read from the source since the last restart.
    private static final class RecordingInputStream extends FilterInputStream {
        private final byte[] head = new byte[TRAILER_SIZE + 2];
        private long count;

        RecordingInputStream(InputStream in) {
            super(in);
        }

        void restart() {
            count = 0;
        }

        long count() {
            return count;
        }

        int recorded(int i) {
            return head[i] & 0xff;
        }

        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b >= 0) {
                if (count < head.length) {
                    head[(int) count] = (byte) b;
                }
                count++;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = in.read(b, off, len);
            if (read > 0) {
                if (count < head.length) {
                    System.arraycopy(b, off, head, (int) count, (int) Math.min(read, head.length - count));
                }
                count += read;
            }
            return read;
        }
    }
}
