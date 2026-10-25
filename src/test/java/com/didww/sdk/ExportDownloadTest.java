package com.didww.sdk;

import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.support.InterruptingBody;
import com.didww.sdk.support.RawHttpServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ProtocolException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ExportDownloadTest extends BaseTest {
    private static final String CSV = "Date/Time Start (UTC),DID,Duration\n2026-10-01 00:00:00,15550000000,0\n";
    private static final String PATH = "/v3/exports/da15f006.csv.gz";

    @TempDir
    Path dir;

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

    private String url() {
        return origin() + PATH;
    }

    private void stubExport(byte[] body) {
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/octet-stream").withBody(body)));
    }

    private List<String> files() throws IOException {
        try (Stream<Path> list = Files.list(dir)) {
            return list.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
        }
    }

    @Test
    void testDownloadWritesTheFileUnchanged() {
        byte[] gz = gzip(CSV);
        stubExport(gz);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        client.downloadExport(url(), out);

        assertThat(out.toByteArray()).isEqualTo(gz);
        wireMock.verify(getRequestedFor(urlEqualTo(PATH))
                .withHeader("Authorization", equalTo("Bearer " + TEST_ACCESS_TOKEN))
                .withHeader("Accept-Encoding", equalTo("identity"))
                .withHeader("User-Agent", equalTo(SdkVersion.userAgent()))
                .withoutHeader("Accept")
                .withoutHeader("Api-Key"));
    }

    @Test
    void testGzipContentEncodingIsNotDecoded() {
        byte[] gz = gzip(CSV);
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Encoding", "gzip").withBody(gz)));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        client.downloadExport(url(), out);

        assertThat(out.toByteArray()).isEqualTo(gz);
    }

    @Test
    void testCheckExportUrlOrigin() {
        String proxy = "https://proxy.example.com/v3";
        Object[][] cases = {
                {proxy, "https://proxy.example.com/v3/exports/1.csv.gz", true},
                {proxy, "https://api.didww.com/v3/exports/1.csv.gz", true},
                {proxy, "https://api.didww.com:443/v3/exports/1.csv.gz", true},
                {proxy, "https://API.didww.com./v3/exports/1.csv.gz", true},
                {"https://proxy.example.com:443/v3", "https://proxy.example.com/v3/exports/1.csv.gz", true},
                {"https://proxy.example.com./v3", "https://proxy.example.com/v3/exports/1.csv.gz", true},
                {"http://proxy.example.com/v3", "http://proxy.example.com:80/v3/exports/1.csv.gz", true},
                {proxy, "https://other.example.com/v3/exports/1.csv.gz", false},
                {proxy, "https://sandbox-api.didww.com/v3/exports/1.csv.gz", false},
                {proxy, "http://api.didww.com/v3/exports/1.csv.gz", false},
                {proxy, "https://api.didww.com:8443/v3/exports/1.csv.gz", false},
                {proxy, "https://proxy.example.com:8443/v3/exports/1.csv.gz", false},
                {proxy, "http://proxy.example.com:443/v3/exports/1.csv.gz", false},
                {proxy, "https://api.didww.com.evil.example/v3/exports/1.csv.gz", false},
                {proxy, "/v3/exports/1.csv.gz", false},
                {proxy, "ftp://api.didww.com/v3/exports/1.csv.gz", false},
                {proxy, null, false},
        };
        for (Object[] c : cases) {
            DidwwClient production = DidwwClient.builder()
                    .credentials(new DidwwCredentials("test-api-key", DidwwEnvironment.PRODUCTION))
                    .baseUrl((String) c[0])
                    .build();
            String url = (String) c[1];
            if ((Boolean) c[2]) {
                assertThatCode(() -> production.checkExportUrl(url)).as("%s with %s", url, c[0]).doesNotThrowAnyException();
            } else {
                DidwwClientException e = catchThrowableOfType(() -> production.checkExportUrl(url),
                        DidwwClientException.class);
                assertThat(e).as("%s with %s", url, c[0]).isNotNull();
                assertThat(e.getMessage()).contains("does not match the API base URL");
            }
        }
    }

    @Test
    void testUrlOnAnotherOriginIsRefusedWithoutRequest() {
        String other = "http://127.0.0.1:" + (wireMock.port() == 65535 ? 65534 : wireMock.port() + 1) + PATH;

        DidwwClientException e = catchThrowableOfType(() -> client.downloadExport(other, new ByteArrayOutputStream()),
                DidwwClientException.class);

        assertThat(e).isNotNull();
        assertThat(e.getMessage()).contains("does not match the API base URL " + origin());
        assertThat(mints()).isZero();
        assertThat(wireMock.getAllServeEvents()).isEmpty();
    }

    @Test
    void testRedirectIsNotFollowed() {
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", origin() + "/v3/elsewhere.csv.gz")));
        stubFor302Target();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DidwwApiException e = catchThrowableOfType(() -> client.downloadExport(url(), out), DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(302);
        assertThat(out.size()).isZero();
        wireMock.verify(0, getRequestedFor(urlEqualTo("/v3/elsewhere.csv.gz")));
    }

    private void stubFor302Target() {
        wireMock.stubFor(get(urlEqualTo("/v3/elsewhere.csv.gz")).willReturn(ok("redirected")));
    }

    @Test
    void testNoContentIsAnError() {
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(204)));

        DidwwApiException e = catchThrowableOfType(() -> client.downloadExport(url(), new ByteArrayOutputStream()),
                DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(204);
        assertThat(e.getErrors()).hasSize(1);
        assertThat(e.getErrors().get(0).getTitle()).isEqualTo("Unexpected status");
    }

    @Test
    void testJsonApiErrorsAreParsed() {
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(404)
                .withHeader("Content-Type", "application/vnd.api+json")
                .withBody("{\"errors\":[{\"title\":\"Not found\",\"detail\":\"Export file is not found\",\"status\":\"404\"}]}")));

        DidwwApiException e = catchThrowableOfType(() -> client.downloadExport(url(), new ByteArrayOutputStream()),
                DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(404);
        assertThat(e.getErrors()).extracting(DidwwApiException.ApiError::getDetail).containsExactly("Export file is not found");
        assertThat(e).hasMessage("DIDWW API error (HTTP 404): Export file is not found");
    }

    @Test
    void testErrorBodyIsLimitedInSize() {
        StringBuilder big = new StringBuilder("{\"errors\":[{\"detail\":\"real error\"}],\"pad\":\"");
        for (int i = 0; i < (2 << 20); i++) {
            big.append('x');
        }
        big.append("\"}");
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(404).withBody(big.toString())));

        DidwwApiException e = catchThrowableOfType(() -> client.downloadExport(url(), new ByteArrayOutputStream()),
                DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(404);
        assertThat(e.getErrors().get(0).getTitle()).isEqualTo("Unexpected status");
        assertThat(e.getErrors().get(0).getDetail()).hasSize(1 << 20);
    }

    @Test
    void testErrorBodyIsReadWithinADeadline() {
        DidwwClient slow = clientBuilder().readTimeout(Duration.ofSeconds(1)).build();
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(503)
                .withBody(new String(new char[200]).replace('\0', 'x'))
                .withChunkedDribbleDelay(50, 6000)));
        long start = System.nanoTime();

        DidwwApiException e = catchThrowableOfType(() -> slow.downloadExport(url(), new ByteArrayOutputStream()),
                DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(503);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(4));
    }

    @Test
    void testDownloadRetriedAfter401WritesOnlyTheFinalBody() {
        wireMock.stubFor(post(urlEqualTo("/token")).atPriority(1)
                .inScenario("tokens").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(okJson(tokenJson("token-1", API_AUDIENCE))).willSetStateTo("second"));
        wireMock.stubFor(post(urlEqualTo("/token")).atPriority(1)
                .inScenario("tokens").whenScenarioStateIs("second")
                .willReturn(okJson(tokenJson("token-2", API_AUDIENCE))));
        wireMock.stubFor(get(urlEqualTo(PATH)).withHeader("Authorization", equalTo("Bearer token-1"))
                .willReturn(aResponse().withStatus(401).withBody("unauthorized body")));
        byte[] gz = gzip(CSV);
        wireMock.stubFor(get(urlEqualTo(PATH)).withHeader("Authorization", equalTo("Bearer token-2"))
                .willReturn(aResponse().withStatus(200).withBody(gz)));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        client.downloadExport(url(), out);

        assertThat(out.toByteArray()).isEqualTo(gz);
        assertThat(mints()).isEqualTo(2);
    }

    @Test
    void testPathDestinationIsReplacedOnlyOnSuccess() throws IOException {
        Path target = dir.resolve("export.csv.gz");
        Files.write(target, "old".getBytes(StandardCharsets.UTF_8));
        wireMock.stubFor(get(urlEqualTo(PATH)).inScenario("export").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(404)).willSetStateTo("ready"));
        byte[] gz = gzip(CSV);
        wireMock.stubFor(get(urlEqualTo(PATH)).inScenario("export").whenScenarioStateIs("ready")
                .willReturn(aResponse().withStatus(200).withBody(gz)));

        catchThrowableOfType(() -> client.downloadExport(url(), target), DidwwApiException.class);
        assertThat(Files.readString(target)).isEqualTo("old");
        assertThat(files()).containsExactly("export.csv.gz");

        client.downloadExport(url(), target);
        assertThat(Files.readAllBytes(target)).isEqualTo(gz);
        assertThat(files()).containsExactly("export.csv.gz");
    }

    @Test
    void testPathDestinationIsNotCreatedOnError() throws IOException {
        Path target = dir.resolve("export.csv.gz");
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(500)));

        catchThrowableOfType(() -> client.downloadExport(url(), target), DidwwApiException.class);

        assertThat(files()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testUnwritableDestinationThrowsUncheckedIOException(boolean decompress) throws IOException {
        stubExport(gzip(CSV));
        Path target = dir.resolve("missing").resolve("export.csv");

        UncheckedIOException e = catchThrowableOfType(() -> {
            if (decompress) {
                client.downloadAndDecompressExport(url(), target);
            } else {
                client.downloadExport(url(), target);
            }
        }, UncheckedIOException.class);

        assertThat(e).hasCauseInstanceOf(NoSuchFileException.class);
        assertThat(files()).isEmpty();
    }

    @Test
    void testTruncatedBodyKeepsTheExistingFile() throws IOException {
        byte[] answer = ("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: 1000\r\n\r\n"
                + "0123456789").getBytes(StandardCharsets.ISO_8859_1);
        Path target = dir.resolve("export.csv.gz");
        Files.write(target, "old".getBytes(StandardCharsets.UTF_8));
        try (RawHttpServer server = RawHttpServer.respondingWith(answer)) {
            DidwwClient raw = clientBuilder().baseUrl(server.url() + "/v3").build();

            DidwwClientException e = catchThrowableOfType(() -> raw.downloadExport(server.url() + PATH, target),
                    DidwwClientException.class);

            assertThat(e).isNotNull();
            assertThat(e.getMessage()).startsWith("Failed to read export data");
            assertThat(e.getCause()).isInstanceOf(ProtocolException.class);
            assertThat(server.heads().get(0)).contains("Authorization: Bearer " + TEST_ACCESS_TOKEN)
                    .doesNotContain("Api-Key");
        }
        assertThat(Files.readString(target)).isEqualTo("old");
        assertThat(files()).containsExactly("export.csv.gz");
    }

    @Test
    void testPlainDownloadCutAtAChunkBoundaryIsAReadFailure() throws IOException {
        byte[] answer = ("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nTransfer-Encoding: chunked\r\n\r\n"
                + "a\r\n0123456789\r\n").getBytes(StandardCharsets.ISO_8859_1);
        try (RawHttpServer server = RawHttpServer.respondingWith(answer)) {
            DidwwClient raw = clientBuilder().baseUrl(server.url() + "/v3").build();

            DidwwClientException e = catchThrowableOfType(
                    () -> raw.downloadExport(server.url() + PATH, new ByteArrayOutputStream()),
                    DidwwClientException.class);

            assertThat(e).isNotNull();
            assertThat(e).hasMessage("Failed to read export data: the download was cut short");
            assertThat(e.getCause()).isInstanceOf(EOFException.class);
        }
    }

    // The thread is interrupted while the body is copied to the file (before its
    // 64 KiB buffer is written) or after the last read (before the commit).
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testInterruptedPathDownloadKeepsTheExistingFile(boolean atEnd) throws IOException {
        stubExport(new byte[atEnd ? 1024 : 200 * 1024]);
        DidwwClient interrupting = clientBuilder()
                .httpClientBuilder(new OkHttpClient.Builder().addInterceptor(InterruptingBody.interceptor(PATH, atEnd)))
                .build();
        Path target = dir.resolve("export.csv.gz");
        Files.write(target, "old".getBytes(StandardCharsets.UTF_8));

        try {
            DidwwClientException e = catchThrowableOfType(() -> interrupting.downloadExport(url(), target),
                    DidwwClientException.class);

            assertThat(e).isNotNull();
            assertThat(e).hasMessage("Export download was interrupted");
            assertThat(e).hasCauseInstanceOf(ClosedByInterruptException.class);
            assertThat(Thread.currentThread().isInterrupted()).as("the interrupt flag stays set").isTrue();
        } finally {
            Thread.interrupted();
        }
        assertThat(Files.readString(target)).isEqualTo("old");
        assertThat(files()).containsExactly("export.csv.gz");
    }

    @Test
    void testBodyIsStreamedAsItArrives() throws IOException {
        CountDownLatch firstWrite = new CountDownLatch(1);
        AtomicBoolean streamed = new AtomicBoolean();
        byte[] half = new byte[64 * 1024];
        Arrays.fill(half, (byte) 'a');
        try (RawHttpServer server = RawHttpServer.start(connection -> {
            connection.readRequest();
            connection.write("HTTP/1.1 200 OK\r\nContent-Length: " + (half.length * 2) + "\r\n\r\n");
            connection.write(half);
            streamed.set(firstWrite.await(5, TimeUnit.SECONDS));
            connection.write(half);
            connection.drain(1000);
        })) {
            DidwwClient raw = clientBuilder().baseUrl(server.url() + "/v3").build();
            ByteArrayOutputStream received = new ByteArrayOutputStream();
            OutputStream out = new OutputStream() {
                @Override
                public void write(int b) {
                    received.write(b);
                    firstWrite.countDown();
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    received.write(b, off, len);
                    firstWrite.countDown();
                }
            };

            raw.downloadExport(server.url() + PATH, out);

            assertThat(streamed).isTrue();
            assertThat(received.size()).isEqualTo(half.length * 2);
        }
    }

    @Test
    void testDestinationWriteFailurePropagates() {
        stubExport(gzip(CSV));
        OutputStream failing = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("disk full");
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                throw new IOException("disk full");
            }
        };

        UncheckedIOException e = catchThrowableOfType(() -> client.downloadExport(url(), failing),
                UncheckedIOException.class);

        assertThat(e).isNotNull();
        assertThat(e.getCause()).hasMessage("disk full");
    }

    @Test
    void testDownloadAndDecompressToStream() {
        stubExport(gzip(CSV));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        client.downloadAndDecompressExport(url(), out);

        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo(CSV);
    }

    @Test
    void testDownloadAndDecompressToPath() throws IOException {
        stubExport(gzip(CSV));
        Path target = dir.resolve("export.csv");
        Files.write(target, "old".getBytes(StandardCharsets.UTF_8));

        client.downloadAndDecompressExport(url(), target);

        assertThat(Files.readString(target)).isEqualTo(CSV);
        assertThat(files()).containsExactly("export.csv");
    }

    // The second gzip member is sent only after the first one was written out,
    // so the decompressor reaches the end of the first member with no data at hand.
    @Test
    void testDecompressReadsAMemberThatArrivesLater() throws IOException {
        byte[] first = gzip("Date/Time Start (UTC),DID,Duration\n");
        byte[] second = gzip("2026-10-01 00:00:00,15550000000,0\n");
        CountDownLatch firstWritten = new CountDownLatch(1);
        try (RawHttpServer server = RawHttpServer.start(connection -> {
            connection.readRequest();
            connection.write("HTTP/1.1 200 OK\r\nContent-Length: " + (first.length + second.length) + "\r\n\r\n");
            connection.write(first);
            firstWritten.await(5, TimeUnit.SECONDS);
            connection.write(second);
            connection.drain(1000);
        })) {
            DidwwClient raw = clientBuilder().baseUrl(server.url() + "/v3").build();
            ByteArrayOutputStream received = new ByteArrayOutputStream();
            OutputStream out = new OutputStream() {
                @Override
                public void write(int b) {
                    received.write(b);
                    firstWritten.countDown();
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    received.write(b, off, len);
                    firstWritten.countDown();
                }
            };

            raw.downloadAndDecompressExport(server.url() + PATH, out);

            assertThat(received.toString(StandardCharsets.UTF_8)).isEqualTo(CSV);
        }
    }

    // The connection closes 5 bytes into the header of the second member, which
    // arrives after the first one was read: GZIPInputStream takes that read
    // failure for the end of the data.
    @Test
    void testDecompressCutInTheHeaderOfALaterMemberKeepsTheExistingFile() throws IOException {
        byte[] first = gzip("Date/Time Start (UTC),DID,Duration\n");
        byte[] second = gzip("2026-10-01 00:00:00,15550000000,0\n");
        Path target = dir.resolve("export.csv");
        Files.write(target, "old".getBytes(StandardCharsets.UTF_8));
        try (RawHttpServer server = RawHttpServer.start(connection -> {
            connection.readRequest();
            connection.write("HTTP/1.1 200 OK\r\nContent-Length: " + (first.length + second.length) + "\r\n\r\n");
            connection.write(first);
            Thread.sleep(500);
            connection.write(Arrays.copyOf(second, 5));
        })) {
            DidwwClient raw = clientBuilder().baseUrl(server.url() + "/v3").build();

            DidwwClientException e = catchThrowableOfType(
                    () -> raw.downloadAndDecompressExport(server.url() + PATH, target), DidwwClientException.class);

            assertThat(e).isNotNull();
            assertThat(e).hasMessage("Failed to read export data: unexpected end of stream");
            assertThat(e.getCause()).isInstanceOf(ProtocolException.class);
        }
        assertThat(Files.readString(target)).isEqualTo("old");
        assertThat(files()).containsExactly("export.csv");
    }

    @Test
    void testCorruptGzipFailsWithoutCreatingTheFile() throws IOException {
        stubExport("not gzip at all".getBytes(StandardCharsets.UTF_8));
        Path target = dir.resolve("export.csv");

        DidwwClientException e = catchThrowableOfType(() -> client.downloadAndDecompressExport(url(), target),
                DidwwClientException.class);

        assertThat(e).isNotNull();
        assertThat(e.getMessage()).startsWith("Failed to decompress export data");
        assertThat(files()).isEmpty();
    }

    @Test
    void testTruncatedGzipFailsAndKeepsTheExistingFile() throws IOException {
        byte[] gz = gzip(CSV);
        stubExport(Arrays.copyOf(gz, gz.length - 8));
        Path target = dir.resolve("export.csv");
        Files.write(target, "old".getBytes(StandardCharsets.UTF_8));

        DidwwClientException e = catchThrowableOfType(() -> client.downloadAndDecompressExport(url(), target),
                DidwwClientException.class);

        assertThat(e).isNotNull();
        assertThat(e.getMessage()).startsWith("Failed to decompress export data");
        assertThat(Files.readString(target)).isEqualTo("old");
        assertThat(files()).containsExactly("export.csv");
    }
}
