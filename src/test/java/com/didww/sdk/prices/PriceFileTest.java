package com.didww.sdk.prices;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.SdkVersion;
import com.didww.sdk.exception.DidwwPricesException;
import com.didww.sdk.exception.PricesErrorKind;
import com.didww.sdk.support.InterruptingBody;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ProtocolException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class PriceFileTest extends PricesTestBase {
    private static final String UNAUTHORIZED_BODY =
            "{\"errors\":[{\"title\":\"Unauthorized\",\"detail\":\"Authorization failed\",\"code\":\"401\",\"status\":\"401\"}]}";
    private static final byte[] OLD_COPY = "old copy".getBytes(StandardCharsets.US_ASCII);

    @TempDir
    Path dir;

    private byte[] fixture;

    private void serveFile() {
        fixture = voiceInFixture();
        wireMock.stubFor(any(urlPathMatching("/v3/.*prices\\.csv\\.gz")).willReturn(priceFile(fixture, LAST_MODIFIED_HEADER)));
    }

    private void stubFile(com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder response) {
        wireMock.stubFor(any(urlPathEqualTo(VOICE_IN_FILE)).willReturn(response));
    }

    private List<String> entries() throws IOException {
        try (Stream<Path> list = Files.list(dir)) {
            return list.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
        }
    }

    private Path oldCopy(String name, Instant modified) throws IOException {
        Path path = dir.resolve(name);
        Files.write(path, OLD_COPY);
        if (modified != null) {
            Files.setLastModifiedTime(path, FileTime.from(modified));
        }
        return path;
    }

    static Stream<Arguments> endpoints() {
        return Stream.of(
                Arguments.of((Function<DidwwClient, PricesService<?>>) DidwwClient::voiceInPrices, "/v3/prices.csv.gz", VOICE_IN),
                Arguments.of((Function<DidwwClient, PricesService<?>>) DidwwClient::voiceOutPrices, "/v3/prices.csv.gz", VOICE_OUT),
                Arguments.of((Function<DidwwClient, PricesService<?>>) DidwwClient::smsOutA2pPrices, "/v3/sms_out/a2p/prices.csv.gz", SMS),
                Arguments.of((Function<DidwwClient, PricesService<?>>) DidwwClient::smsOutP2pPrices, "/v3/sms_out/p2p/prices.csv.gz", SMS),
                Arguments.of((Function<DidwwClient, PricesService<?>>) DidwwClient::smsOutOtpPrices, "/v3/sms_out/otp/prices.csv.gz", SMS),
                Arguments.of((Function<DidwwClient, PricesService<?>>) DidwwClient::smsInPrices, "/v3/sms_in/prices.csv.gz", SMS));
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    void testPriceFileEndpoints(Function<DidwwClient, PricesService<?>> service, String path, String audience) {
        serveFile();
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        service.apply(client).download(destination);

        List<LoggedRequest> requests = priceRequests();
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getMethod().getName()).isEqualTo("GET");
        assertThat(requests.get(0).getUrl()).isEqualTo(path);
        assertThat(requests.get(0).getHeader("Authorization")).isEqualTo("Bearer " + TEST_ACCESS_TOKEN);
        assertThat(mintAudiences()).containsExactly(audience);
        assertThat(destination.toByteArray()).isEqualTo(fixture);
    }

    @Test
    void testPriceFileDownload() {
        serveFile();
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        PriceFile file = client.voiceInPrices().download(destination);

        assertThat(destination.toByteArray()).as("the gzipped bytes are written unchanged").isEqualTo(fixture);
        assertThat(file.getStatusCode()).isEqualTo(200);
        assertThat(file.isNotModified()).isFalse();
        assertThat(file.getLastModified()).isEqualTo(LAST_MODIFIED);
        assertThat(file.getFileName()).isEqualTo("voice_in_prices.csv.gz");
        assertThat(file.getBytesWritten()).isEqualTo(fixture.length);
        assertThat(file.toString()).contains("statusCode=200", "voice_in_prices.csv.gz");

        LoggedRequest request = priceRequests().get(0);
        assertThat(request.getHeader("Accept-Encoding")).isEqualTo("identity");
        assertThat(request.getHeader("User-Agent")).isEqualTo(SdkVersion.userAgent());
        assertThat(request.containsHeader("If-Modified-Since")).isFalse();
        assertThat(request.containsHeader("If-None-Match")).isFalse();
        assertThat(request.containsHeader("Api-Key")).isFalse();
        assertThat(request.containsHeader("Accept")).isFalse();
    }

    @Test
    void testPriceFileDownloadSendsIfModifiedSince() {
        serveFile();

        client.voiceInPrices().download(OutputStream.nullOutputStream(), Instant.parse("2026-10-01T08:05:03.900Z"));

        assertThat(priceRequests().get(0).getHeader("If-Modified-Since"))
                .as("IMF-fixdate with a two-digit day, in whole seconds")
                .isEqualTo("Thu, 01 Oct 2026 08:05:03 GMT");
    }

    @Test
    void testPriceFileDownloadNotModified() {
        stubFile(notModified(LAST_MODIFIED_HEADER));
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        PriceFile file = client.voiceInPrices().download(destination, LAST_MODIFIED);

        assertThat(file.isNotModified()).isTrue();
        assertThat(file.getStatusCode()).isEqualTo(304);
        assertThat(file.getLastModified()).isEqualTo(LAST_MODIFIED);
        assertThat(file.getContentLength()).isNull();
        assertThat(file.getBytesWritten()).isZero();
        assertThat(destination.size()).isZero();
        assertThat(priceRequests().get(0).getHeader("If-Modified-Since")).isEqualTo(LAST_MODIFIED_HEADER);
    }

    @Test
    void testPriceFileDownloadRequiresDestination() {
        serveFile();

        NullPointerException e = catchThrowableOfType(() -> client.voiceInPrices().download(null),
                NullPointerException.class);

        assertThat(e).hasMessageContaining("destination");
        assertThat(priceRequests()).isEmpty();
    }

    @Test
    void testPriceFileHead() {
        serveFile();

        PriceFile file = client.voiceInPrices().head();

        LoggedRequest request = priceRequests().get(0);
        assertThat(request.getMethod().getName()).isEqualTo("HEAD");
        assertThat(request.getUrl()).isEqualTo("/v3/prices.csv.gz");
        assertThat(request.getHeader("Accept-Encoding")).isEqualTo("identity");
        assertThat(request.containsHeader("If-Modified-Since")).isFalse();
        assertThat(file.getStatusCode()).isEqualTo(200);
        assertThat(file.getLastModified()).isEqualTo(LAST_MODIFIED);
        assertThat(file.getFileName()).isEqualTo("voice_in_prices.csv.gz");
        assertThat(file.getBytesWritten()).isZero();
    }

    @Test
    void testPriceFileHeadNotModified() {
        stubFile(notModified(LAST_MODIFIED_HEADER));

        PriceFile file = client.voiceInPrices().head(LAST_MODIFIED);

        assertThat(file.isNotModified()).isTrue();
        assertThat(file.getLastModified()).isEqualTo(LAST_MODIFIED);
        assertThat(priceRequests().get(0).getHeader("If-Modified-Since")).isEqualTo(LAST_MODIFIED_HEADER);
    }

    @Test
    void testPriceFileHeadNotFoundWithoutBody() {
        stubFile(aResponse().withStatus(404));

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().head(), DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED);
        assertThat(e.getHttpStatus()).isEqualTo(404);
        assertThat(e.getCode()).isEqualTo("price_plan_not_assigned");
        assertThat(e.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Wed, 14 Oct 2026 06:00:00 GMT",
            "Thu, 14 Oct 2026 06:00:00 GMT",
            "Wednesday, 14-Oct-26 06:00:00 GMT",
            "Wed Oct 14 06:00:00 2026",
            "Wed, 14 Oct 2026 08:00:00 +0200",
            "Wed, 14 oct 2026 06:00:00 GMT",
            "not a date"
    })
    void testPriceFileLastModifiedIsImfFixdateOnly(String header) {
        stubFile(priceFile(new byte[0], header));

        PriceFile file = client.voiceInPrices().head();

        assertThat(file.getLastModified())
                .isEqualTo(header.equals(LAST_MODIFIED_HEADER) ? LAST_MODIFIED : null);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '~', value = {
            "attachment; filename=\"voice_in_prices.csv.gz\"|voice_in_prices.csv.gz",
            "attachment; filename=plain.csv.gz|plain.csv.gz",
            "attachment; filename=\"a\\\"b.csv.gz\"|a\"b.csv.gz",
            "attachment; filename=\"fallback.csv.gz\"; filename*=UTF-8''pr%C3%AFces%20a+b.csv.gz|pr\u00efces a+b.csv.gz",
            "attachment; filename*=utf-8'en'x%2Fy.csv.gz|x/y.csv.gz",
            "attachment; filename=\"fallback.csv.gz\"; filename*=KOI8-R''x.csv.gz|fallback.csv.gz",
            "attachment; filename=\"fallback.csv.gz\"; filename*=UTF-8''bad%zz|fallback.csv.gz",
            "attachment; filename=\"\"|",
            "attachment|"
    })
    void testPriceFileName(String header, String fileName) {
        stubFile(aResponse().withStatus(200).withHeader("Content-Disposition", header));

        assertThat(client.voiceInPrices().head().getFileName()).isEqualTo(fileName);
    }

    @Test
    void testPriceFileHeadRetriesServiceUnavailable() {
        fixture = voiceInFixture();
        stubSequence(VOICE_IN_FILE, aResponse().withStatus(503).withHeader("Retry-After", "2"),
                priceFile(fixture, LAST_MODIFIED_HEADER));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        RecordedWaits waits = RecordedWaits.stub(prices, 0);

        PriceFile file = prices.head();

        assertThat(file.getStatusCode()).isEqualTo(200);
        assertThat(waits.all()).containsExactly(Duration.ofSeconds(2));
        assertThat(priceRequests()).extracting(r -> r.getMethod().getName()).containsExactly("HEAD", "HEAD");
    }

    @Test
    void testPriceFileDownloadRetriesServiceUnavailable() {
        fixture = voiceInFixture();
        stubSequence(VOICE_IN_FILE, servingFailure("1"), priceFile(fixture, LAST_MODIFIED_HEADER));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        RecordedWaits waits = RecordedWaits.stub(prices, 0);
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        prices.download(destination);

        assertThat(destination.toByteArray()).as("the 503 body is not written").isEqualTo(fixture);
        assertThat(waits.all()).containsExactly(Duration.ofSeconds(1));
        assertThat(priceRequests()).hasSize(2);
    }

    @Test
    void testPriceFileDownloadServiceUnavailableAfterTwoRetries() {
        stubFile(servingFailure("1"));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        RecordedWaits waits = RecordedWaits.stub(prices, 0);
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        DidwwPricesException e = catchThrowableOfType(() -> prices.download(destination), DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.SERVICE_UNAVAILABLE);
        assertThat(e.getHttpStatus()).isEqualTo(503);
        assertThat(e.getCode()).isEqualTo("serving_failure");
        assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(1));
        assertThat(e).hasMessage("Prices service: The price file is not available yet.");
        assertThat(priceRequests()).hasSize(3);
        assertThat(waits.all()).containsExactly(Duration.ofSeconds(1), Duration.ofSeconds(1));
        assertThat(destination.size()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"2, 0, 2", "2, 1, 3", "30, 0, 30", "30, 1, 36", "60, 1, 72", "1, 0.5, 1.5"})
    void testPriceFileServiceUnavailableWaitIsSpread(String retryAfter, double jitter, double waitSeconds) {
        stubSequence(VOICE_IN_FILE, servingFailure(retryAfter), priceFile(new byte[0], LAST_MODIFIED_HEADER));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        RecordedWaits waits = RecordedWaits.stub(prices, jitter);

        prices.head();

        assertThat(waits.all()).containsExactly(Duration.ofMillis((long) (waitSeconds * 1000)));
    }

    // OkHttp itself sends a request once more, without waiting, when a 503
    // carries Retry-After: 0; the SDK then sees the second 503 and waits a
    // second (plus jitter) before its own retry.
    @ParameterizedTest
    @CsvSource({"0, 1", "1, 2"})
    void testPriceFileServiceUnavailableRetryAfterZero(double jitter, long waitSeconds) {
        stubSequence(VOICE_IN_FILE, servingFailure("0"), servingFailure("0"), priceFile(new byte[0], LAST_MODIFIED_HEADER));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        RecordedWaits waits = RecordedWaits.stub(prices, jitter);

        PriceFile file = prices.head();

        assertThat(file.getStatusCode()).isEqualTo(200);
        assertThat(waits.all()).containsExactly(Duration.ofSeconds(waitSeconds));
        assertThat(priceRequests()).as("two requests by OkHttp, then the SDK retry").hasSize(3);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {"61|61", "120|120", "Wed, 21 Oct 2026 07:28:00 GMT|", "|", "-1|", "1.5|"})
    void testPriceFileDownloadServiceUnavailableNotRetried(String retryAfter, Integer retryAfterSeconds) {
        stubFile(servingFailure(retryAfter));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        RecordedWaits waits = RecordedWaits.stub(prices, 0);

        DidwwPricesException e = catchThrowableOfType(() -> prices.download(OutputStream.nullOutputStream()),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.SERVICE_UNAVAILABLE);
        assertThat(e.getRetryAfter()).isEqualTo(retryAfterSeconds != null ? Duration.ofSeconds(retryAfterSeconds) : null);
        assertThat(priceRequests()).hasSize(1);
        assertThat(waits.all()).isEmpty();
    }

    @Test
    void testPriceFileDownloadSleepInterrupted() {
        stubFile(servingFailure("1"));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        InterruptedException interrupted = new InterruptedException("sleep interrupted");
        prices.setSleeper(wait -> {
            throw interrupted;
        });

        try {
            DidwwPricesException e = catchThrowableOfType(() -> prices.download(OutputStream.nullOutputStream()),
                    DidwwPricesException.class);

            assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
            assertThat(e.getHttpStatus()).isEqualTo(503);
            assertThat(e.getCause()).isSameAs(interrupted);
            assertThat(Thread.currentThread().isInterrupted()).as("the interrupt flag is restored").isTrue();
            assertThat(priceRequests()).hasSize(1);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void testPriceFileDownloadTooManyRequestsNotRetried() {
        stubFile(aResponse().withStatus(429).withHeader("Retry-After", "30"));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        RecordedWaits waits = RecordedWaits.stub(prices, 0);

        DidwwPricesException e = catchThrowableOfType(() -> prices.download(OutputStream.nullOutputStream()),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.TOO_MANY_REQUESTS);
        assertThat(e.getHttpStatus()).isEqualTo(429);
        assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(30));
        assertThat(priceRequests()).hasSize(1);
        assertThat(waits.all()).isEmpty();
    }

    @Test
    void testPriceFileDownloadRetriesRefusedTokenOnce() {
        fixture = voiceInFixture();
        stubTwoTokens(VOICE_IN);
        wireMock.stubFor(get(urlPathEqualTo(VOICE_IN_FILE)).withHeader("Authorization", equalTo("Bearer token-1"))
                .willReturn(jsonApi(401, UNAUTHORIZED_BODY)));
        wireMock.stubFor(get(urlPathEqualTo(VOICE_IN_FILE)).withHeader("Authorization", equalTo("Bearer token-2"))
                .willReturn(priceFile(fixture, LAST_MODIFIED_HEADER)));
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        client.voiceInPrices().download(destination);

        assertThat(destination.toByteArray()).as("only the file bytes are written").isEqualTo(fixture);
        assertThat(priceRequests()).extracting(r -> r.getHeader("Authorization"))
                .containsExactly("Bearer token-1", "Bearer token-2");
        assertThat(mintAudiences()).containsExactly(VOICE_IN, VOICE_IN);
    }

    @Test
    void testPriceFileDownloadToFileNotAuthorized() throws IOException {
        stubFile(jsonApi(401, UNAUTHORIZED_BODY));

        DidwwPricesException e = catchThrowableOfType(
                () -> client.voiceInPrices().downloadToFile(dir.resolve("voice_in_prices.csv.gz")),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.NOT_AUTHORIZED);
        assertThat(e.getHttpStatus()).isEqualTo(401);
        assertThat(e.getCode()).isEqualTo("401");
        assertThat(e).hasMessage("Prices service: Authorization failed");
        assertThat(entries()).isEmpty();
    }

    @Test
    void testPriceFileDownloadPricePlanNotAssigned() {
        stubFile(jsonApi(404, fixture("prices/errors/price_plan_not_assigned.json")));
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().download(destination),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED);
        assertThat(e).hasMessage("Prices service: No price plan is assigned to this account.");
        assertThat(destination.size()).isZero();
    }

    @Test
    void testPriceFileDownloadAccessDenied() {
        stubFile(jsonApi(403, "{\"errors\":[{\"title\":\"Insufficient scope\",\"code\":\"403\",\"status\":\"403\"}]}"));

        DidwwPricesException e = catchThrowableOfType(
                () -> client.voiceInPrices().download(OutputStream.nullOutputStream()), DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.ACCESS_DENIED);
        assertThat(e).hasMessage("Prices service: Insufficient scope");
        assertThat(priceRequests()).as("a 403 is never retried").hasSize(1);
        assertThat(mints()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {202, 204, 301, 302, 307})
    void testPriceFileDownloadUnexpectedStatus(int status) throws IOException {
        stubFile(aResponse().withStatus(status).withHeader("Location", "/v3/elsewhere.csv.gz"));
        wireMock.stubFor(get(urlPathEqualTo("/v3/elsewhere.csv.gz")).willReturn(priceFile(voiceInFixture(),
                LAST_MODIFIED_HEADER)));

        DidwwPricesException e = catchThrowableOfType(
                () -> client.voiceInPrices().downloadToFile(dir.resolve("voice_in_prices.csv.gz")),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.UNEXPECTED_STATUS);
        assertThat(e.getHttpStatus()).isEqualTo(status);
        assertThat(e).hasMessage("Prices service responded with status " + status);
        assertThat(priceRequests()).as("redirects are not followed").hasSize(1);
        assertThat(entries()).isEmpty();
    }

    @Test
    void testPriceFileDownloadDestinationError() {
        serveFile();
        IOException writeError = new IOException("disk full");

        UncheckedIOException e = catchThrowableOfType(
                () -> client.voiceInPrices().download(new FailingOutputStream(writeError)), UncheckedIOException.class);

        assertThat(e.getCause()).as("the destination's exception is the cause").isSameAs(writeError);
    }

    @Test
    void testPriceFileDownloadBrokenBodyKeepsExistingFile() throws IOException {
        stubFile(aResponse().withStatus(200).withFault(Fault.MALFORMED_RESPONSE_CHUNK));
        Path path = oldCopy("voice_in_prices.csv.gz", null);

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().downloadToFile(path),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
        assertThat(e.getHttpStatus()).isEqualTo(200);
        assertThat(e).hasCauseInstanceOf(IOException.class);
        assertThat(Files.readAllBytes(path)).isEqualTo(OLD_COPY);
        assertThat(entries()).containsExactly("voice_in_prices.csv.gz");
    }

    @Test
    void testPriceFileDownloadConnectionResetKeepsExistingFile() throws IOException {
        stubFile(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER));
        Path path = oldCopy("voice_in_prices.csv.gz", null);

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().downloadToFile(path),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
        assertThat(e.getHttpStatus()).isNull();
        assertThat(Files.readAllBytes(path)).isEqualTo(OLD_COPY);
        assertThat(entries()).containsExactly("voice_in_prices.csv.gz");
    }

    // The thread is interrupted while the body is copied to the file (before its
    // 64 KiB buffer is written) or after the last read (before the commit).
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testPriceFileDownloadToFileInterruptedKeepsExistingFile(boolean atEnd) throws IOException {
        byte[] body = new byte[atEnd ? 1024 : 200 * 1024];
        stubFile(priceFile(body, LAST_MODIFIED_HEADER));
        DidwwClient interrupting = builder()
                .httpClientBuilder(new OkHttpClient.Builder().addInterceptor(InterruptingBody.interceptor(VOICE_IN_FILE, atEnd)))
                .build();
        Path path = oldCopy("voice_in_prices.csv.gz", null);

        try {
            DidwwPricesException e = catchThrowableOfType(() -> interrupting.voiceInPrices().downloadToFile(path),
                    DidwwPricesException.class);

            assertThat(e).isNotNull();
            assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
            assertThat(e.getHttpStatus()).isEqualTo(200);
            assertThat(e).hasCauseInstanceOf(ClosedByInterruptException.class);
            assertThat(Thread.currentThread().isInterrupted()).as("the interrupt flag stays set").isTrue();
        } finally {
            Thread.interrupted();
        }
        assertThat(Files.readAllBytes(path)).isEqualTo(OLD_COPY);
        assertThat(entries()).containsExactly("voice_in_prices.csv.gz");
    }

    @Test
    void testPriceFileDownloadConnectionRefused() {
        DidwwClient refused = builder().voiceInPricesUrl(closedUrl()).build();

        DidwwPricesException e = catchThrowableOfType(() -> refused.voiceInPrices().download(OutputStream.nullOutputStream()),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
        assertThat(e.getHttpStatus()).isNull();
        assertThat(e).hasCauseInstanceOf(IOException.class);
    }

    @Test
    void testPriceFileDownloadChecksContentLength() {
        // A network interceptor that swaps the body for a shorter one: the
        // Content-Length check catches what OkHttp's own framing cannot.
        serveFile();
        DidwwClient swapping = builder().httpClientBuilder(new OkHttpClient.Builder().addNetworkInterceptor(chain -> {
            Response response = chain.proceed(chain.request());
            if (!chain.request().url().encodedPath().endsWith(".csv.gz")) {
                return response;
            }
            response.close();
            return response.newBuilder().header("Content-Length", "1000")
                    .body(ResponseBody.create(new byte[10], null)).build();
        })).build();
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        DidwwPricesException e = catchThrowableOfType(() -> swapping.voiceInPrices().download(destination),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
        assertThat(e.getHttpStatus()).isEqualTo(200);
        assertThat(e.getCause()).isInstanceOf(ProtocolException.class).hasMessage("incomplete body: 10 of 1000 bytes");
    }

    @Test
    void testPriceFileRefusesAnswerFromAnotherUrl() {
        // A user interceptor that follows a redirect by itself: the answer of
        // another URL is never written.
        serveFile();
        wireMock.stubFor(any(urlPathEqualTo("/v3/elsewhere.csv.gz")).willReturn(priceFile(voiceInFixture(),
                LAST_MODIFIED_HEADER)));
        DidwwClient following = builder().httpClientBuilder(new OkHttpClient.Builder().addInterceptor(chain -> {
            Request request = chain.request();
            if (!request.url().encodedPath().endsWith("prices.csv.gz")) {
                return chain.proceed(request);
            }
            return chain.proceed(request.newBuilder()
                    .url(request.url().newBuilder().encodedPath("/v3/elsewhere.csv.gz").build()).build());
        })).build();
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        DidwwPricesException e = catchThrowableOfType(() -> following.voiceInPrices().download(destination),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.UNEXPECTED_STATUS);
        assertThat(e.getMessage()).contains("redirected");
        assertThat(destination.size()).isZero();
    }

    @Test
    void testPriceFileErrorBodyIsCutAtOneMebibyte() {
        // Cut at 1 MiB, the body is no JSON and its price_not_found code is
        // not seen: the 404 then means no price plan.
        StringBuilder body = new StringBuilder("{\"errors\":[{\"code\":\"price_not_found\"}],\"pad\":\"");
        char[] pad = new char[2 << 20];
        Arrays.fill(pad, 'x');
        body.append(pad).append("\"}");
        stubFile(jsonApi(404, body.toString()));

        DidwwPricesException e = catchThrowableOfType(
                () -> client.voiceInPrices().download(OutputStream.nullOutputStream()), DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED);
        assertThat(e.getErrors()).isEmpty();
    }

    @Test
    void testPriceFileErrorBodyIsReadWithinItsDeadline() {
        stubFile(jsonApi(404, fixture("prices/errors/price_not_found.json")).withChunkedDribbleDelay(50, 5000));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        prices.setErrorBodyDeadline(Duration.ofMillis(500));
        long started = System.nanoTime();

        DidwwPricesException e = catchThrowableOfType(() -> prices.download(OutputStream.nullOutputStream()),
                DidwwPricesException.class);

        assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(3));
        assertThat(e.getHttpStatus()).as("the status still decides").isEqualTo(404);
        assertThat(e.getKind()).isEqualTo(PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED);
    }

    @Test
    void testPriceFileDownloadToFile() throws IOException {
        serveFile();
        Path path = oldCopy("voice_in_prices.csv.gz", null);

        PriceFile file = client.voiceInPrices().downloadToFile(path);

        assertThat(Files.readAllBytes(path)).isEqualTo(fixture);
        assertThat(Files.getLastModifiedTime(path).toInstant()).as("modification time is Last-Modified")
                .isEqualTo(LAST_MODIFIED);
        assertThat(entries()).containsExactly("voice_in_prices.csv.gz");
        assertThat(file.getStatusCode()).isEqualTo(200);
        assertThat(file.getBytesWritten()).isEqualTo(fixture.length);
        assertThat(file.getFileName()).isEqualTo("voice_in_prices.csv.gz");
    }

    @Test
    void testPriceFileDownloadToFileWithoutLastModified() throws IOException {
        fixture = voiceInFixture();
        stubFile(aResponse().withStatus(200).withBody(fixture));
        Path path = dir.resolve("voice_in_prices.csv.gz");

        PriceFile file = client.voiceInPrices().downloadToFile(path);

        assertThat(file.getLastModified()).isNull();
        assertThat(Files.readAllBytes(path)).isEqualTo(fixture);
    }

    @Test
    void testPriceFileDownloadToFileLeavesOtherTempFiles() throws IOException {
        serveFile();
        Path path = dir.resolve("voice_in_prices.csv.gz");
        Files.write(dir.resolve("voice_in_prices.csv.gz.tmp"), "concurrent download".getBytes(StandardCharsets.UTF_8));
        Files.write(dir.resolve(".voice_in_prices.csv.gz.0123456789abcdef.tmp"), new byte[]{1});

        client.voiceInPrices().downloadToFile(path);

        assertThat(Files.readAllBytes(path)).isEqualTo(fixture);
        assertThat(Files.readString(dir.resolve("voice_in_prices.csv.gz.tmp"))).isEqualTo("concurrent download");
        assertThat(entries()).containsExactly(".voice_in_prices.csv.gz.0123456789abcdef.tmp",
                "voice_in_prices.csv.gz", "voice_in_prices.csv.gz.tmp");
    }

    @Test
    void testPriceFileDownloadToFileErrorKeepsExistingFile() throws IOException {
        stubFile(jsonApi(404, fixture("prices/errors/price_plan_not_assigned.json")));
        Path path = oldCopy("voice_in_prices.csv.gz", null);

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().downloadToFile(path),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED);
        assertThat(Files.readAllBytes(path)).isEqualTo(OLD_COPY);
        assertThat(entries()).containsExactly("voice_in_prices.csv.gz");
    }

    @Test
    void testPriceFileDownloadToFileMissingDirectory() {
        serveFile();

        UncheckedIOException e = catchThrowableOfType(
                () -> client.voiceInPrices().downloadToFile(dir.resolve("missing").resolve("prices.csv.gz")),
                UncheckedIOException.class);

        assertThat(e.getCause()).isInstanceOf(NoSuchFileException.class);
        assertThat(priceRequests()).isEmpty();
    }

    @Test
    void testPriceFileFetchWithoutCopy() throws IOException {
        serveFile();
        Path path = dir.resolve("voice_in_prices.csv.gz");

        PriceFile file = client.voiceInPrices().fetch(path);

        assertThat(file.getStatusCode()).isEqualTo(200);
        assertThat(Files.readAllBytes(path)).isEqualTo(fixture);
        assertThat(priceRequests().get(0).containsHeader("If-Modified-Since")).isFalse();
    }

    @Test
    void testPriceFileFetchSendsModificationTime() throws IOException {
        stubFile(notModified(LAST_MODIFIED_HEADER));
        Path path = oldCopy("voice_in_prices.csv.gz", LAST_MODIFIED.plusMillis(700));

        PriceFile file = client.voiceInPrices().fetch(path);

        assertThat(file.isNotModified()).isTrue();
        assertThat(priceRequests().get(0).getHeader("If-Modified-Since")).isEqualTo(LAST_MODIFIED_HEADER);
        assertThat(Files.readAllBytes(path)).isEqualTo(OLD_COPY);
        assertThat(Files.getLastModifiedTime(path).toInstant()).isEqualTo(LAST_MODIFIED.plusMillis(700));
        assertThat(entries()).containsExactly("voice_in_prices.csv.gz");
    }

    @Test
    void testPriceFileFetchReplacesChangedCopy() throws IOException {
        fixture = voiceInFixture();
        stubSequence(VOICE_IN_FILE,
                priceFile("previous copy".getBytes(StandardCharsets.US_ASCII), LAST_MODIFIED_HEADER),
                priceFile(fixture, OTHER_LAST_MODIFIED_HEADER),
                notModified(OTHER_LAST_MODIFIED_HEADER));
        PricesService<VoiceInPrice> prices = client.voiceInPrices();
        Path path = dir.resolve("voice_in_prices.csv.gz");

        for (int i = 0; i < 3; i++) {
            prices.fetch(path);
        }

        List<LoggedRequest> requests = priceRequests();
        assertThat(requests).hasSize(3);
        assertThat(requests.get(0).containsHeader("If-Modified-Since")).isFalse();
        assertThat(requests.get(1).getHeader("If-Modified-Since")).isEqualTo(LAST_MODIFIED_HEADER);
        assertThat(requests.get(2).getHeader("If-Modified-Since")).isEqualTo(OTHER_LAST_MODIFIED_HEADER);
        assertThat(requests).allSatisfy(r -> assertThat(r.containsHeader("If-None-Match")).isFalse());
        assertThat(Files.readAllBytes(path)).isEqualTo(fixture);
        assertThat(Files.getLastModifiedTime(path).toInstant()).isEqualTo(OTHER_LAST_MODIFIED);
        assertThat(entries()).containsExactly("voice_in_prices.csv.gz");
    }

    @Test
    void testPriceFileNeverSendsIfNoneMatch() {
        serveFile();
        DidwwClient etag = builder().httpClientBuilder(new OkHttpClient.Builder().addInterceptor(chain ->
                chain.proceed(chain.request().newBuilder().header("If-None-Match", "\"abc\"").build()))).build();

        etag.voiceInPrices().download(OutputStream.nullOutputStream(), LAST_MODIFIED);

        LoggedRequest request = priceRequests().get(0);
        assertThat(request.containsHeader("If-None-Match")).isFalse();
        assertThat(request.getHeader("If-Modified-Since")).isEqualTo(LAST_MODIFIED_HEADER);
    }
}
