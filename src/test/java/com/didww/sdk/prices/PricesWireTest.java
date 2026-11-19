package com.didww.sdk.prices;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.exception.DidwwPricesException;
import com.didww.sdk.exception.PricesErrorKind;
import com.didww.sdk.support.RawHttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Price requests over real sockets: the token service is WireMock, the prices
 * host a raw HTTP server that sends exactly the bytes given.
 */
class PricesWireTest extends PricesTestBase {
    private static final Map<String, String> NUMBER = Collections.singletonMap("number", "18005550123");

    @TempDir
    Path dir;

    private static String fileHeaders(int status, String reason, Integer contentLength) {
        return "HTTP/1.1 " + status + " " + reason + "\r\n"
                + "Content-Type: application/gzip\r\n"
                + "Content-Disposition: attachment; filename=\"voice_in_prices.csv.gz\"\r\n"
                + "Last-Modified: " + LAST_MODIFIED_HEADER + "\r\n"
                + (contentLength != null ? "Content-Length: " + contentLength + "\r\n" : "")
                + "Connection: close\r\n\r\n";
    }

    private static byte[] concat(String head, byte[] body) {
        byte[] headBytes = head.getBytes(StandardCharsets.ISO_8859_1);
        byte[] all = new byte[headBytes.length + body.length];
        System.arraycopy(headBytes, 0, all, 0, headBytes.length);
        System.arraycopy(body, 0, all, headBytes.length, body.length);
        return all;
    }

    private static boolean isHead(String requestHead) {
        return requestHead.startsWith("HEAD ");
    }

    private static Map<String, String> headers(String requestHead) {
        return Stream.of(requestHead.split("\r\n")).skip(1).filter(line -> line.indexOf(':') > 0)
                .collect(Collectors.toMap(line -> line.substring(0, line.indexOf(':')).trim().toLowerCase(),
                        line -> line.substring(line.indexOf(':') + 1).trim(), (a, b) -> b));
    }

    /** Serves the Voice IN fixture with its Content-Length, the headers only to HEAD. */
    private RawHttpServer servePriceFile(String contentEncoding) throws IOException {
        byte[] fixture = voiceInFixture();
        return RawHttpServer.start(connection -> {
            String head = connection.readRequest();
            String headers = fileHeaders(200, "OK", fixture.length);
            if (contentEncoding != null) {
                headers = headers.replace("Connection: close", "Content-Encoding: " + contentEncoding + "\r\nConnection: close");
            }
            connection.write(isHead(head) ? headers.getBytes(StandardCharsets.ISO_8859_1) : concat(headers, fixture));
            connection.drain(200);
        });
    }

    private DidwwClient client(RawHttpServer prices) {
        return builder().voiceInPricesUrl(prices.url()).build();
    }

    @Test
    void testPriceFileDownloadOverHttp() throws IOException {
        try (RawHttpServer prices = servePriceFile(null)) {
            ByteArrayOutputStream destination = new ByteArrayOutputStream();

            PriceFile file = client(prices).voiceInPrices().download(destination, LAST_MODIFIED.plusSeconds(7200));

            assertThat(destination.toByteArray()).isEqualTo(voiceInFixture());
            assertThat(file.getStatusCode()).isEqualTo(200);
            assertThat(file.getContentLength()).isEqualTo((long) voiceInFixture().length);
            assertThat(file.getLastModified()).isEqualTo(LAST_MODIFIED);
            assertThat(file.getFileName()).isEqualTo("voice_in_prices.csv.gz");
            List<String> heads = prices.heads();
            assertThat(heads).hasSize(1);
            assertThat(heads.get(0)).startsWith("GET /v3/prices.csv.gz HTTP/1.1\r\n");
            Map<String, String> headers = headers(heads.get(0));
            assertThat(headers).containsEntry("authorization", "Bearer " + TEST_ACCESS_TOKEN)
                    .containsEntry("if-modified-since", "Wed, 14 Oct 2026 08:00:00 GMT")
                    .containsEntry("accept-encoding", "identity")
                    .doesNotContainKeys("if-none-match", "accept", "api-key");
        }
    }

    @Test
    void testPriceFileGzipEncodingIsKeptRaw() throws IOException {
        // Content-Encoding: gzip would make OkHttp decompress the body and drop
        // its length if the SDK let it ask for gzip.
        try (RawHttpServer prices = servePriceFile("gzip")) {
            ByteArrayOutputStream destination = new ByteArrayOutputStream();

            PriceFile file = client(prices).voiceInPrices().download(destination);

            assertThat(destination.toByteArray()).isEqualTo(voiceInFixture());
            assertThat(file.getContentLength()).isEqualTo((long) voiceInFixture().length);
            assertThat(file.getBytesWritten()).isEqualTo(voiceInFixture().length);
        }
    }

    @Test
    void testPriceFileHeadOverHttp() throws IOException {
        try (RawHttpServer prices = servePriceFile(null)) {
            PriceFile file = client(prices).voiceInPrices().head();

            assertThat(prices.heads()).hasSize(1);
            assertThat(prices.heads().get(0)).startsWith("HEAD /v3/prices.csv.gz ");
            assertThat(file.getStatusCode()).isEqualTo(200);
            assertThat(file.getContentLength()).as("HEAD carries the Content-Length of the file")
                    .isEqualTo((long) voiceInFixture().length);
            assertThat(file.getLastModified()).isEqualTo(LAST_MODIFIED);
            assertThat(file.getBytesWritten()).isZero();
        }
    }

    @Test
    void testPriceFileLargeContentLength() throws IOException {
        try (RawHttpServer prices = RawHttpServer.start(connection -> {
            connection.readRequest();
            connection.write(fileHeaders(200, "OK", null).replace("Connection: close",
                    "Content-Length: 5000000000\r\nConnection: close"));
            connection.drain(200);
        })) {
            PriceFile file = client(prices).voiceInPrices().head();

            assertThat(file.getContentLength()).as("files over 2 GiB").isEqualTo(5_000_000_000L);
        }
    }

    @Test
    void testPriceFileNotModifiedOverHttp() throws IOException {
        try (RawHttpServer prices = RawHttpServer.respondingWith(
                fileHeaders(304, "Not Modified", null).getBytes(StandardCharsets.ISO_8859_1))) {
            ByteArrayOutputStream destination = new ByteArrayOutputStream();

            PriceFile file = client(prices).voiceInPrices().download(destination, LAST_MODIFIED);

            assertThat(file.isNotModified()).isTrue();
            assertThat(file.getContentLength()).as("a 304 has no length").isNull();
            assertThat(file.getLastModified()).isEqualTo(LAST_MODIFIED);
            assertThat(destination.size()).isZero();
        }
    }

    @Test
    void testPriceFileDownloadTruncatedBody() throws IOException {
        try (RawHttpServer prices = RawHttpServer.respondingWith(
                (fileHeaders(200, "OK", 1000) + "0123456789").getBytes(StandardCharsets.ISO_8859_1))) {
            Path path = dir.resolve("voice_in_prices.csv.gz");
            Files.write(path, "old copy".getBytes(StandardCharsets.US_ASCII));

            DidwwPricesException e = catchThrowableOfType(() -> client(prices).voiceInPrices().downloadToFile(path),
                    DidwwPricesException.class);

            assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
            assertThat(e.getHttpStatus()).isEqualTo(200);
            assertThat(e).hasCauseInstanceOf(ProtocolException.class);
            assertThat(Files.readString(path)).isEqualTo("old copy");
            try (Stream<Path> entries = Files.list(dir)) {
                assertThat(entries.map(p -> p.getFileName().toString())).containsExactly("voice_in_prices.csv.gz");
            }
        }
    }

    @Test
    void testPriceFileStalledBodyTimesOut() throws IOException {
        try (RawHttpServer prices = RawHttpServer.start(connection -> {
            connection.readRequest();
            connection.write(fileHeaders(200, "OK", 1000) + "0123456789");
            connection.drain(10_000);
        })) {
            DidwwClient slow = builder().voiceInPricesUrl(prices.url()).readTimeout(Duration.ofMillis(500)).build();
            long started = System.nanoTime();

            DidwwPricesException e = catchThrowableOfType(
                    () -> slow.voiceInPrices().download(OutputStream.nullOutputStream()), DidwwPricesException.class);

            assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(5));
            assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
            assertThat(e.getHttpStatus()).isEqualTo(200);
            assertThat(e).hasCauseInstanceOf(SocketTimeoutException.class);
        }
    }

    @Test
    void testLookupTruncatedErrorBodyKeepsStatus() throws IOException {
        try (RawHttpServer prices = RawHttpServer.respondingWith((
                "HTTP/1.1 429 Too Many Requests\r\nContent-Type: application/vnd.api+json\r\nRetry-After: 7\r\n"
                        + "Content-Length: 200\r\nConnection: close\r\n\r\n{\"errors\":[").getBytes(StandardCharsets.ISO_8859_1))) {
            DidwwPricesException e = catchThrowableOfType(() -> client(prices).voiceInPrices().lookup(NUMBER),
                    DidwwPricesException.class);

            assertThat(e.getKind()).isEqualTo(PricesErrorKind.TOO_MANY_REQUESTS);
            assertThat(e.getHttpStatus()).isEqualTo(429);
            assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(7));
        }
    }

    @Test
    void testLookupTruncatedBody() throws IOException {
        try (RawHttpServer prices = RawHttpServer.respondingWith((
                "HTTP/1.1 200 OK\r\nContent-Type: application/vnd.api+json\r\nContent-Length: 200\r\n"
                        + "Connection: close\r\n\r\n{\"data\":[").getBytes(StandardCharsets.ISO_8859_1))) {
            DidwwPricesException e = catchThrowableOfType(() -> client(prices).voiceInPrices().lookup(NUMBER),
                    DidwwPricesException.class);

            assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
            assertThat(e.getHttpStatus()).isEqualTo(200);
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    void testLookupStalledErrorBodyKeepsStatus() throws IOException {
        try (RawHttpServer prices = RawHttpServer.start(connection -> {
            connection.readRequest();
            connection.write("HTTP/1.1 429 Too Many Requests\r\nContent-Type: application/vnd.api+json\r\nRetry-After: 7\r\n"
                    + "Content-Length: 200\r\nConnection: close\r\n\r\n{\"errors\":[");
            connection.drain(10_000);
        })) {
            DidwwClient slow = builder().voiceInPricesUrl(prices.url()).readTimeout(Duration.ofMillis(500)).build();

            DidwwPricesException e = catchThrowableOfType(() -> slow.voiceInPrices().lookup(NUMBER),
                    DidwwPricesException.class);

            assertThat(e.getKind()).isEqualTo(PricesErrorKind.TOO_MANY_REQUESTS);
            assertThat(e.getHttpStatus()).isEqualTo(429);
            assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(7));
        }
    }

    @Test
    void testPriceFileTrickledErrorBodyKeepsStatus() throws IOException {
        // Each byte comes before the read timeout, the whole body not within it.
        try (RawHttpServer prices = RawHttpServer.start(connection -> {
            connection.readRequest();
            connection.write("HTTP/1.1 503 Service Unavailable\r\nContent-Type: application/vnd.api+json\r\n"
                    + "Retry-After: 120\r\nContent-Length: 100\r\nConnection: close\r\n\r\n");
            for (int i = 0; i < 100; i++) {
                connection.write("x");
                Thread.sleep(100);
            }
        })) {
            DidwwClient slow = builder().voiceInPricesUrl(prices.url()).readTimeout(Duration.ofSeconds(1)).build();
            long started = System.nanoTime();

            DidwwPricesException e = catchThrowableOfType(
                    () -> slow.voiceInPrices().download(OutputStream.nullOutputStream()), DidwwPricesException.class);

            assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(5));
            assertThat(e.getKind()).isEqualTo(PricesErrorKind.SERVICE_UNAVAILABLE);
            assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(120));
            assertThat(prices.accepts()).isEqualTo(1);
        }
    }

    @Test
    void testPriceFileDoesNotFollowRedirects() throws IOException {
        byte[] fixture = voiceInFixture();
        try (RawHttpServer prices = RawHttpServer.start(connection -> {
            String head = connection.readRequest();
            connection.write(head.startsWith("GET /v3/prices.csv.gz ")
                    ? ("HTTP/1.1 302 Found\r\nLocation: /v3/elsewhere.csv.gz\r\nContent-Length: 0\r\n"
                    + "Connection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1)
                    : concat(fileHeaders(200, "OK", fixture.length), fixture));
            connection.drain(200);
        })) {
            ByteArrayOutputStream destination = new ByteArrayOutputStream();

            DidwwPricesException e = catchThrowableOfType(() -> client(prices).voiceInPrices().download(destination),
                    DidwwPricesException.class);

            assertThat(e.getKind()).isEqualTo(PricesErrorKind.UNEXPECTED_STATUS);
            assertThat(e.getHttpStatus()).isEqualTo(302);
            assertThat(prices.heads()).hasSize(1);
            assertThat(destination.size()).isZero();
        }
    }

    // OkHttp sends a request once more by itself on a 503 with Retry-After: 0
    // (see PricesService); with the two SDK retries that makes six requests.
    @Test
    void testServiceUnavailableRetryAfterZeroIsAlsoResentByOkHttp() throws IOException {
        try (RawHttpServer prices = RawHttpServer.respondingWith(("HTTP/1.1 503 Service Unavailable\r\n"
                + "Retry-After: 0\r\nContent-Length: 0\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1))) {
            PricesService<VoiceInPrice> service = client(prices).voiceInPrices();
            RecordedWaits waits = RecordedWaits.stub(service, 0);

            DidwwPricesException e = catchThrowableOfType(service::head, DidwwPricesException.class);

            assertThat(e.getKind()).isEqualTo(PricesErrorKind.SERVICE_UNAVAILABLE);
            assertThat(e.getRetryAfter()).isEqualTo(Duration.ZERO);
            assertThat(waits.all()).containsExactly(Duration.ofSeconds(1), Duration.ofSeconds(1));
            assertThat(prices.heads()).hasSize(6);
        }
    }

    @Test
    void testPriceFileConnectionReset() throws IOException {
        try (RawHttpServer prices = RawHttpServer.start(connection -> {
            connection.readRequest();
            connection.socket().setSoLinger(true, 0);
        })) {
            DidwwPricesException e = catchThrowableOfType(
                    () -> client(prices).voiceInPrices().download(OutputStream.nullOutputStream()),
                    DidwwPricesException.class);

            assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
            assertThat(e.getHttpStatus()).isNull();
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }
}
