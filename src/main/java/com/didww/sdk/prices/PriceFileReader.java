package com.didww.sdk.prices;

import com.didww.sdk.internal.MultiMemberGzipInputStream;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads a price file as downloaded (gzipped CSV), one row at a time.
 *
 * <p>Each row is a map keyed by the header line as is (for example
 * {@code Rate (USD, excl. VAT)}), in header order; the values are the CSV
 * fields as is, empty for a missing value. A UTF-8 byte order mark is skipped,
 * invalid UTF-8 reads as U+FFFD, empty lines are skipped, and a file with only
 * a header has no rows.
 *
 * <p>The rows are read lazily while the stream is consumed, so rows before an
 * error have already been passed on. A read error is raised as an
 * {@link UncheckedIOException} whose cause tells what went wrong:
 * {@link java.util.zip.ZipException} for input that is not gzip or has a bad
 * checksum, {@link java.io.EOFException} for gzip data cut short (an empty
 * input included), and an {@link IOException} whose message starts with
 * {@code Price file:} for malformed CSV or a row whose field count differs
 * from the header. Data after the end of the gzip data is ignored, so input
 * of several gzip members that ends within the first bytes of a later member
 * reads as complete.
 */
public final class PriceFileReader {
    private static final int BUFFER_SIZE = 64 * 1024;

    private PriceFileReader() {
    }

    /**
     * Returns the rows of the gzipped price file read from {@code gzipped}.
     * Nothing is read before the stream is consumed. Closing the returned
     * stream releases the decompressor; {@code gzipped} is left open.
     *
     * @param gzipped the price file as downloaded
     * @return the rows, read lazily
     */
    public static Stream<Map<String, String>> rows(InputStream gzipped) {
        Objects.requireNonNull(gzipped, "gzipped must not be null");
        RowIterator rows = new RowIterator(new FilterInputStream(gzipped) {
            @Override
            public void close() {
                // The caller's stream stays open.
            }
        });
        return stream(rows).onClose(rows::closeQuietly);
    }

    /**
     * Returns the rows of the gzipped price file at {@code path}. The file is
     * opened now and closed with the returned stream, so use it in a
     * try-with-resources statement, as with {@link Files#lines(Path)}.
     *
     * @param path the price file as downloaded
     * @return the rows, read lazily
     * @throws IOException if the file cannot be opened
     */
    public static Stream<Map<String, String>> rows(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        InputStream in = Files.newInputStream(path);
        RowIterator rows = new RowIterator(in);
        return stream(rows).onClose(() -> {
            rows.closeQuietly();
            try {
                in.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static Stream<Map<String, String>> stream(RowIterator rows) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(rows, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    private static final class RowIterator implements Iterator<Map<String, String>>, Closeable {
        private final InputStream source;
        private MultiMemberGzipInputStream gzip;
        private CsvRecordReader csv;
        private List<String> header;
        private Map<String, String> next;
        private boolean done;

        RowIterator(InputStream source) {
            this.source = source;
        }

        @Override
        public boolean hasNext() {
            if (next == null && !done) {
                try {
                    next = readRow();
                } catch (IOException e) {
                    done = true;
                    closeQuietly();
                    throw new UncheckedIOException(e);
                }
                if (next == null) {
                    done = true;
                    closeQuietly();
                }
            }
            return next != null;
        }

        @Override
        public Map<String, String> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Map<String, String> row = next;
            next = null;
            return row;
        }

        private Map<String, String> readRow() throws IOException {
            if (csv == null) {
                gzip = new MultiMemberGzipInputStream(source, BUFFER_SIZE);
                BufferedReader text = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));
                text.mark(1);
                if (text.read() != '\ufeff') {
                    text.reset();
                }
                csv = new CsvRecordReader(text);
                header = csv.read();
            }
            if (header == null) {
                return null;
            }
            List<String> record = csv.read();
            if (record == null) {
                return null;
            }
            if (record.size() != header.size()) {
                throw new IOException("Price file: record on line " + csv.recordLine() + ": wrong number of fields");
            }
            Map<String, String> row = new LinkedHashMap<>(header.size() * 2);
            for (int i = 0; i < header.size(); i++) {
                row.put(header.get(i), record.get(i));
            }
            return row;
        }

        void closeQuietly() {
            try {
                close();
            } catch (IOException ignored) {
                // Nothing more to read.
            }
        }

        @Override
        public void close() throws IOException {
            done = true;
            if (gzip != null) {
                gzip.close();
            }
        }
    }
}
