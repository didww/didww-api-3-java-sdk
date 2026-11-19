package com.didww.sdk.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class MultiMemberGzipInputStreamTest {
    private static final String FIRST = "Country,Rate\nA,1\n";
    private static final String SECOND = "B,2\nC,3\n";

    private static byte[] gzip(String text) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
                gz.write(text.getBytes(StandardCharsets.UTF_8));
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] both = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    // The data as a byte array, and like a network stream: available() is 0
    // and reads return at most maxRead bytes.
    private static Map<String, InputStream> sources(byte[] data) {
        Map<String, InputStream> sources = new LinkedHashMap<>();
        sources.put("byte array", new ByteArrayInputStream(data));
        for (int maxRead : new int[] {1, 7, 64, Integer.MAX_VALUE}) {
            sources.put("reads of " + maxRead, new FilterInputStream(new ByteArrayInputStream(data)) {
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return super.read(b, off, Math.min(len, maxRead));
                }

                @Override
                public int available() {
                    return 0;
                }
            });
        }
        return sources;
    }

    private static String readAll(InputStream source) throws IOException {
        try (InputStream in = new MultiMemberGzipInputStream(source, 64 * 1024)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void testReadsASingleMember() throws IOException {
        for (Map.Entry<String, InputStream> source : sources(gzip(FIRST)).entrySet()) {
            assertThat(readAll(source.getValue())).as(source.getKey()).isEqualTo(FIRST);
        }
    }

    @Test
    void testReadsEveryMember() throws IOException {
        for (Map.Entry<String, InputStream> source : sources(concat(gzip(FIRST), gzip(SECOND))).entrySet()) {
            assertThat(readAll(source.getValue())).as(source.getKey()).isEqualTo(FIRST + SECOND);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 18, 100})
    void testIgnoresZerosAfterTheLastMember(int padding) throws IOException {
        for (Map.Entry<String, InputStream> source : sources(concat(gzip(FIRST), new byte[padding])).entrySet()) {
            assertThat(readAll(source.getValue())).as(source.getKey()).isEqualTo(FIRST);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"xyz", "\u001f\u0000", "\u008b\u001f"})
    void testIgnoresOtherDataAfterTheLastMember(String trailing) throws IOException {
        byte[] data = concat(gzip(FIRST), trailing.getBytes(StandardCharsets.ISO_8859_1));
        for (Map.Entry<String, InputStream> source : sources(data).entrySet()) {
            assertThat(readAll(source.getValue())).as(source.getKey()).isEqualTo(FIRST);
        }
    }

    // GZIPInputStream takes a member cut within its first bytes for the end of the data.
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19})
    void testRejectsALaterMemberCutShort(int cut) {
        byte[] data = concat(gzip(FIRST), Arrays.copyOf(gzip(SECOND), cut));
        for (Map.Entry<String, InputStream> source : sources(data).entrySet()) {
            assertThat(catchThrowableOfType(() -> readAll(source.getValue()), EOFException.class))
                    .as(source.getKey()).isNotNull();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 8})
    void testRejectsALaterMemberCutInItsTrailer(int cut) {
        byte[] second = gzip(SECOND);
        byte[] data = concat(gzip(FIRST), Arrays.copyOf(second, second.length - cut));
        for (Map.Entry<String, InputStream> source : sources(data).entrySet()) {
            assertThat(catchThrowableOfType(() -> readAll(source.getValue()), EOFException.class))
                    .as(source.getKey()).isNotNull();
        }
    }

    @Test
    void testRejectsALaterMemberWithACorruptHeader() {
        byte[] second = gzip(SECOND);
        second[2] = 7;
        for (Map.Entry<String, InputStream> source : sources(concat(gzip(FIRST), second)).entrySet()) {
            assertThat(catchThrowableOfType(() -> readAll(source.getValue()), ZipException.class))
                    .as(source.getKey()).isNotNull();
        }
    }
}
