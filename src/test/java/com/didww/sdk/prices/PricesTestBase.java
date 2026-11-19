package com.didww.sdk.prices;

import com.didww.sdk.BaseTest;
import com.didww.sdk.DidwwClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.ScenarioMappingBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/** WireMock-backed price tests: every prices host is the WireMock server. */
abstract class PricesTestBase extends BaseTest {
    static final String VOICE_IN = "api-vin.didww.com";
    static final String VOICE_OUT = "api-vout.didww.com";
    static final String SMS = "api-sms.didww.com";

    static final String LAST_MODIFIED_HEADER = "Wed, 14 Oct 2026 06:00:00 GMT";
    static final Instant LAST_MODIFIED = Instant.parse("2026-10-14T06:00:00Z");
    static final String OTHER_LAST_MODIFIED_HEADER = "Thu, 15 Oct 2026 06:00:00 GMT";
    static final Instant OTHER_LAST_MODIFIED = Instant.parse("2026-10-15T06:00:00Z");

    static final String VOICE_IN_FILE = "/v3/prices.csv.gz";
    static final String VOICE_IN_LOOKUP = "/v3/lookup/prices";

    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeEach
    void stubPriceTokens() {
        stubToken(VOICE_IN, TEST_ACCESS_TOKEN);
        stubToken(VOICE_OUT, TEST_ACCESS_TOKEN);
        stubToken(SMS, TEST_ACCESS_TOKEN);
    }

    byte[] voiceInFixture() {
        return loadFixtureBytes("prices/voice_in_prices.csv.gz");
    }

    String fixture(String path) {
        return loadFixture(path);
    }

    /** A price file answer, as the services send it. */
    static ResponseDefinitionBuilder priceFile(byte[] body, String lastModified) {
        return aResponse().withStatus(200)
                .withHeader("Content-Type", "application/gzip")
                .withHeader("Content-Disposition", "attachment; filename=\"voice_in_prices.csv.gz\"")
                .withHeader("Last-Modified", lastModified)
                .withHeader("Cache-Control", "private")
                .withBody(body);
    }

    static ResponseDefinitionBuilder notModified(String lastModified) {
        return aResponse().withStatus(304)
                .withHeader("Content-Disposition", "attachment; filename=\"voice_in_prices.csv.gz\"")
                .withHeader("Last-Modified", lastModified);
    }

    static ResponseDefinitionBuilder jsonApi(int status, String body) {
        return aResponse().withStatus(status).withHeader("Content-Type", "application/vnd.api+json").withBody(body);
    }

    ResponseDefinitionBuilder servingFailure(String retryAfter) {
        ResponseDefinitionBuilder response = jsonApi(503, fixture("prices/errors/serving_failure.json"));
        return retryAfter != null ? response.withHeader("Retry-After", retryAfter) : response;
    }

    /** Answers GET and HEAD of {@code path} with the responses in turn, the last one from then on. */
    void stubSequence(String path, ResponseDefinitionBuilder... responses) {
        for (int i = 0; i < responses.length; i++) {
            ScenarioMappingBuilder mapping = any(urlPathEqualTo(path)).inScenario("sequence " + path)
                    .whenScenarioStateIs(i == 0 ? Scenario.STARTED : "answer " + i);
            if (i < responses.length - 1) {
                mapping = mapping.willSetStateTo("answer " + (i + 1));
            }
            wireMock.stubFor(mapping.willReturn(responses[i]));
        }
    }

    /** The requests other than token requests, in order. */
    List<LoggedRequest> priceRequests() {
        List<LoggedRequest> requests = new ArrayList<>(wireMock.findAll(anyRequestedFor(urlPathMatching("/(?!token).*"))));
        requests.sort((a, b) -> a.getLoggedDate().compareTo(b.getLoggedDate()));
        return requests;
    }

    /** The audiences of every token request, in order. */
    List<String> mintAudiences() {
        List<LoggedRequest> mints = new ArrayList<>(wireMock.findAll(postRequestedFor(urlEqualTo("/token"))));
        mints.sort((a, b) -> a.getLoggedDate().compareTo(b.getLoggedDate()));
        List<String> audiences = new ArrayList<>();
        for (LoggedRequest mint : mints) {
            try {
                for (JsonNode aud : JSON.readTree(mint.getBodyAsString()).get("aud")) {
                    audiences.add(aud.asText());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return audiences;
    }

    /** Answers token requests for {@code audience} with token-1, then token-2 from the second request on. */
    void stubTwoTokens(String audience) {
        wireMock.stubFor(post(urlEqualTo("/token")).inScenario("tokens " + audience)
                .whenScenarioStateIs(Scenario.STARTED)
                .withRequestBody(matchingJsonPath("$.aud[0]", equalTo(audience)))
                .willSetStateTo("second")
                .willReturn(okJson(tokenJson("token-1", audience))));
        wireMock.stubFor(post(urlEqualTo("/token")).inScenario("tokens " + audience)
                .whenScenarioStateIs("second")
                .withRequestBody(matchingJsonPath("$.aud[0]", equalTo(audience)))
                .willReturn(okJson(tokenJson("token-2", audience))));
    }

    DidwwClient.Builder builder() {
        return clientBuilder();
    }

    static String closedUrl() {
        try (ServerSocket socket = new ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))) {
            return "http://127.0.0.1:" + socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static byte[] gzip(String text) {
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

    /** Records the 503 waits of a service instead of sleeping. */
    static final class RecordedWaits {
        private final List<Duration> waits = Collections.synchronizedList(new ArrayList<>());

        static RecordedWaits stub(PricesService<?> service, double jitter) {
            RecordedWaits recorded = new RecordedWaits();
            service.setRandom(() -> jitter);
            service.setSleeper(recorded.waits::add);
            return recorded;
        }

        List<Duration> all() {
            synchronized (waits) {
                return new ArrayList<>(waits);
            }
        }
    }

    /** An output stream whose writes fail. */
    static final class FailingOutputStream extends OutputStream {
        final IOException error;

        FailingOutputStream(IOException error) {
            this.error = error;
        }

        @Override
        public void write(int b) throws IOException {
            throw error;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            throw error;
        }
    }
}
