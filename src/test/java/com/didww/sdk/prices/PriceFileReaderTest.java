package com.didww.sdk.prices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class PriceFileReaderTest {

    @TempDir
    Path dir;

    private static byte[] fixture(String name) {
        try (InputStream in = PriceFileReaderTest.class.getClassLoader()
                .getResourceAsStream("fixtures/prices/" + name)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

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

    private static List<Map<String, String>> readAll(byte[] gzipped) {
        return readAll(new ByteArrayInputStream(gzipped));
    }

    private static List<Map<String, String>> readAll(InputStream gzipped) {
        try (Stream<Map<String, String>> rows = PriceFileReader.rows(gzipped)) {
            return rows.collect(Collectors.toList());
        }
    }

    private static Map<String, String> row(String... pairs) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            row.put(pairs[i], pairs[i + 1]);
        }
        return row;
    }

    @Test
    void testRowsVoiceIn() {
        List<Map<String, String>> rows = readAll(fixture("voice_in_prices.csv.gz"));

        assertThat(rows).hasSize(4);
        assertThat(rows.get(0)).containsExactlyEntriesOf(row(
                "Country", "United Kingdom",
                "Src country name", "",
                "Source type", "cellular",
                "Destination", "44800",
                "Rate (USD, excl. VAT)", "",
                "Connection fee (USD, excl. VAT)", "",
                "Initial interval", "60",
                "Next interval", "60",
                "Reject calls", "true"));
        assertThat(rows).extracting(r -> r.get("Destination")).containsExactly("44800", "4480012", "1800", "1800");
    }

    @Test
    void testRowsVoiceOut() {
        List<Map<String, String>> rows = readAll(fixture("voice_out_prices.csv.gz"));

        assertThat(rows).hasSize(4);
        assertThat(rows.get(0)).containsExactlyEntriesOf(row(
                "Category", "International",
                "Country", "Germany",
                "Network", "Fixed",
                "Prefix", "4930",
                "Rate", "0.0045",
                "Connection fee", "0.0",
                "Initial Increment", "1",
                "Recurring Increment", "1",
                "Reject calls", "false",
                "Next rate", "0.005",
                "Next connection fee", "0.0",
                "Next initial increment", "30",
                "Next recurring increment", "6",
                "Effective date", "2026-11-01T00:00:00Z",
                "Comment", "Increase and Billing increment change"));
        assertThat(rows.get(1).get("Category")).isEqualTo("Origin based");
        assertThat(rows.get(2).get("Prefix")).isEqualTo("99950,99951,99952");
        assertThat(rows.get(3).get("Network")).isEqualTo("New Breakout");
        assertThat(rows.get(3).get("Rate")).isEmpty();
        assertThat(rows.get(3).get("Reject calls")).isEqualTo("true");
    }

    @Test
    void testRowsFromPath() throws IOException {
        Path path = dir.resolve("voice_in_prices.csv.gz");
        Files.write(path, fixture("voice_in_prices.csv.gz"));

        try (Stream<Map<String, String>> rows = PriceFileReader.rows(path)) {
            assertThat(rows.map(r -> r.get("Source type"))).containsExactly("cellular", "fixed", "cellular", "fixed");
        }
        Files.delete(path);
        assertThat(Files.exists(path)).isFalse();
    }

    @Test
    void testRowsFromMissingPathFailsAtOnce() {
        assertThat(catchThrowableOfType(() -> PriceFileReader.rows(dir.resolve("missing.csv.gz")),
                NoSuchFileException.class)).isNotNull();
    }

    @Test
    void testRowsAreReadLazily() {
        byte[] notGzip = "Country,Destination\n".getBytes(StandardCharsets.UTF_8);

        assertThatCode(() -> PriceFileReader.rows(new ByteArrayInputStream(notGzip)).close())
                .as("nothing is read before the stream is consumed").doesNotThrowAnyException();
    }

    @Test
    void testRowsPropagateConsumerException() {
        IllegalStateException stop = new IllegalStateException("stop");
        AtomicInteger calls = new AtomicInteger();

        IllegalStateException e = catchThrowableOfType(() -> {
            try (Stream<Map<String, String>> rows = PriceFileReader.rows(
                    new ByteArrayInputStream(fixture("voice_in_prices.csv.gz")))) {
                rows.forEach(r -> {
                    calls.incrementAndGet();
                    throw stop;
                });
            }
        }, IllegalStateException.class);

        assertThat(e).isSameAs(stop);
        assertThat(calls).hasValue(1);
    }

    @Test
    void testRowsRejectUncompressedInput() {
        UncheckedIOException e = catchThrowableOfType(
                () -> readAll("Country,Destination\nGermany,49\n".getBytes(StandardCharsets.UTF_8)),
                UncheckedIOException.class);

        assertThat(e.getCause()).isInstanceOf(ZipException.class);
    }

    @Test
    void testRowsRejectBadChecksum() {
        byte[] bytes = fixture("voice_in_prices.csv.gz").clone();
        bytes[bytes.length - 8] ^= 0x01;

        UncheckedIOException e = catchThrowableOfType(() -> readAll(bytes), UncheckedIOException.class);

        assertThat(e.getCause()).isInstanceOf(ZipException.class);
    }

    @ParameterizedTest
    @CsvSource({"voice_in_prices.csv.gz, 1", "voice_in_prices.csv.gz, 4", "voice_in_prices.csv.gz, 8",
            "voice_in_prices.csv.gz, 9", "voice_out_prices.csv.gz, 1", "voice_in_prices.csv.gz, -2",
            "voice_in_prices.csv.gz, 0"})
    void testRowsRejectTruncatedGzip(String name, int cut) {
        byte[] bytes = fixture(name);
        byte[] truncated = cut == 0 ? new byte[0]
                : cut < 0 ? Arrays.copyOf(bytes, bytes.length / -cut)
                : Arrays.copyOf(bytes, bytes.length - cut);

        UncheckedIOException e = catchThrowableOfType(() -> readAll(truncated), UncheckedIOException.class);

        assertThat(e.getCause()).isInstanceOf(EOFException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7, 8192})
    void testRowsAcceptShortReads(int maxRead) {
        String csv = "Prefix,Rate\n" + IntStream.range(0, 5000)
                .mapToObj(i -> i + "," + String.format(java.util.Locale.ROOT, "0.%06d", i)).collect(Collectors.joining("\n")) + "\n";
        byte[] gzipped = gzip(csv);
        InputStream chunked = new FilterInputStream(new ByteArrayInputStream(gzipped)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, maxRead));
            }
        };

        List<Map<String, String>> rows;
        try (Stream<Map<String, String>> stream = PriceFileReader.rows(chunked)) {
            rows = stream.collect(Collectors.toList());
        }

        assertThat(rows).hasSize(5000);
        assertThat(rows.get(4999).get("Rate")).isEqualTo("0.004999");
    }

    @Test
    void testRowsReadConcatenatedMembers() throws IOException {
        ByteArrayOutputStream both = new ByteArrayOutputStream();
        both.write(gzip("A,B\n1,2\n"));
        both.write(gzip("3,4\n"));

        assertThat(readAll(both.toByteArray())).containsExactly(row("A", "1", "B", "2"), row("A", "3", "B", "4"));
    }

    // Like a network stream: available() is 0, and a read ends with the first member.
    @Test
    void testRowsReadConcatenatedMembersOfAStreamWithNothingAvailable() throws IOException {
        byte[] first = gzip("A,B\n1,2\n");
        ByteArrayOutputStream both = new ByteArrayOutputStream();
        both.write(first);
        both.write(gzip("3,4\n"));
        AtomicInteger position = new AtomicInteger();
        InputStream network = new FilterInputStream(new ByteArrayInputStream(both.toByteArray())) {
            @Override
            public int read() throws IOException {
                int b = super.read();
                if (b >= 0) {
                    position.incrementAndGet();
                }
                return b;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int limit = position.get() < first.length ? first.length - position.get() : len;
                int read = super.read(b, off, Math.min(len, limit));
                if (read > 0) {
                    position.addAndGet(read);
                }
                return read;
            }

            @Override
            public int available() {
                return 0;
            }
        };

        List<Map<String, String>> rows;
        try (Stream<Map<String, String>> stream = PriceFileReader.rows(network)) {
            rows = stream.collect(Collectors.toList());
        }

        assertThat(rows).containsExactly(row("A", "1", "B", "2"), row("A", "3", "B", "4"));
    }

    // GZIPInputStream takes a member cut within its first bytes for the end of the data.
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 9, 10, 18, 19})
    void testRowsRejectALaterMemberCutShort(int cut) {
        byte[] data = concat(gzip("A,B\n1,2\n"), Arrays.copyOf(gzip("3,4\n5,6\n"), cut));
        for (int maxRead : new int[] {1, 7, 64, Integer.MAX_VALUE}) {
            InputStream network = new FilterInputStream(new ByteArrayInputStream(data)) {
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return super.read(b, off, Math.min(len, maxRead));
                }

                @Override
                public int available() {
                    return 0;
                }
            };

            UncheckedIOException e = catchThrowableOfType(() -> readAll(network), UncheckedIOException.class);

            assertThat(e).as("reads of %d", maxRead).isNotNull();
            assertThat(e.getCause()).isInstanceOf(EOFException.class);
        }
        UncheckedIOException e = catchThrowableOfType(() -> readAll(data), UncheckedIOException.class);

        assertThat(e).isNotNull();
        assertThat(e.getCause()).isInstanceOf(EOFException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"xyz", "\u0000", "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"})
    void testRowsIgnoreOtherDataAfterTheLastMember(String trailing) {
        byte[] data = concat(gzip("A,B\n1,2\n"), trailing.getBytes(StandardCharsets.ISO_8859_1));

        assertThat(readAll(data)).containsExactly(row("A", "1", "B", "2"));
    }

    // Like a network stream, the first read ends with the first member; GZIPInputStream
    // takes the read failure in the header of the second one for the end of the data.
    @Test
    void testRowsRejectAReadFailureInTheHeaderOfALaterMember() throws IOException {
        byte[] first = gzip("A,B\n1,2\n");
        ByteArrayOutputStream sent = new ByteArrayOutputStream();
        sent.write(first);
        sent.write(gzip("3,4\n"), 0, 5);
        int cut = sent.size();
        AtomicInteger position = new AtomicInteger();
        InputStream network = new InputStream() {
            private final byte[] bytes = sent.toByteArray();

            @Override
            public int read() throws IOException {
                byte[] one = new byte[1];
                return read(one, 0, 1) < 0 ? -1 : one[0] & 0xff;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (position.get() >= cut) {
                    throw new IOException("connection reset");
                }
                int limit = position.get() < first.length ? first.length : cut;
                int read = Math.min(len, limit - position.get());
                System.arraycopy(bytes, position.get(), b, off, read);
                position.addAndGet(read);
                return read;
            }
        };
        List<Map<String, String>> rows = new java.util.ArrayList<>();

        UncheckedIOException e = catchThrowableOfType(() -> {
            try (Stream<Map<String, String>> stream = PriceFileReader.rows(network)) {
                stream.forEach(rows::add);
            }
        }, UncheckedIOException.class);

        assertThat(e).isNotNull();
        assertThat(e.getCause()).isInstanceOf(IOException.class).hasMessage("connection reset");
        assertThat(rows).containsExactly(row("A", "1", "B", "2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\ufeffCountry,Rate\nUS,0.1\n", "\ufeff\"Country\",Rate\nUS,0.1\n"})
    void testRowsSkipByteOrderMark(String csv) {
        List<Map<String, String>> rows = readAll(gzip(csv));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).containsEntry("Country", "US");
    }

    @Test
    void testRowsReplaceInvalidUtf8() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write("Name\nA".getBytes(StandardCharsets.UTF_8));
            gz.write(0xff);
            gz.write("B\n".getBytes(StandardCharsets.UTF_8));
        }

        assertThat(readAll(out.toByteArray()).get(0).get("Name")).isEqualTo("A\ufffdB");
    }

    @Test
    void testRowsQuotedFields() {
        String csv = "\"Prefix\",\"Rate (USD, excl. VAT)\",Comment\r\n"
                + "\"99950,99951\",0.25,\"say \"\"hi\"\"\"\r\n"
                + "\r\n"
                + "49,,\"two\r\nlines\"\r\n"
                + " 1 ,\"\",last";

        List<Map<String, String>> rows = readAll(gzip(csv));

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0)).containsExactlyEntriesOf(row(
                "Prefix", "99950,99951",
                "Rate (USD, excl. VAT)", "0.25",
                "Comment", "say \"hi\""));
        assertThat(rows.get(1).get("Comment")).isEqualTo("two\nlines");
        assertThat(rows.get(1).get("Rate (USD, excl. VAT)")).isEmpty();
        assertThat(rows.get(2).get("Prefix")).as("spaces are kept").isEqualTo(" 1 ");
        assertThat(rows.get(2).get("Comment")).as("the last line needs no line end").isEqualTo("last");
    }

    @Test
    void testRowsDuplicateHeaderKeepsLastValue() {
        assertThat(readAll(gzip("A,A,B\n1,2,3\n")).get(0)).containsExactlyEntriesOf(row("A", "2", "B", "3"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '~', value = {
            "A,B\\n1,2,3\\n|Price file: record on line 2: wrong number of fields",
            "A,B\\n1\\n|Price file: record on line 2: wrong number of fields",
            "A,B\\n\\n1,2\\n3\\n|Price file: record on line 4: wrong number of fields",
            "A,B\\n1,x\"y\\n|Price file: parse error on line 2: bare \" in non-quoted field",
            "A,B\\n1,\"x\"y\\n|Price file: parse error on line 2: extraneous or missing \" in quoted-field",
            "A,B\\n1,\"open\\n|Price file: parse error on line 2: extraneous or missing \" in quoted-field"
    })
    void testRowsRejectMalformedCsv(String csv, String message) {
        String text = csv.replace("\\n", "\n");

        UncheckedIOException e = catchThrowableOfType(() -> readAll(gzip(text)), UncheckedIOException.class);

        assertThat(e.getCause()).isExactlyInstanceOf(IOException.class).hasMessage(message);
    }

    @Test
    void testRowsBeforeAnErrorArePassedOn() {
        List<String> seen = new java.util.ArrayList<>();

        catchThrowableOfType(() -> {
            try (Stream<Map<String, String>> rows = PriceFileReader.rows(
                    new ByteArrayInputStream(gzip("A,B\n1,2\n3\n")))) {
                rows.forEach(r -> seen.add(r.get("A")));
            }
        }, UncheckedIOException.class);

        assertThat(seen).containsExactly("1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Country,Rate\n", "\n\n"})
    void testRowsWithoutRows(String csv) {
        assertThat(readAll(gzip(csv))).isEmpty();
    }

    @Test
    void testRowsLeaveInputOpen() {
        AtomicBoolean closed = new AtomicBoolean();
        InputStream in = new FilterInputStream(new ByteArrayInputStream(fixture("voice_in_prices.csv.gz"))) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        try (Stream<Map<String, String>> rows = PriceFileReader.rows(in)) {
            assertThat(rows.count()).isEqualTo(4);
        }

        assertThat(closed).isFalse();
    }
}
