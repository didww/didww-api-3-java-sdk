package com.didww.sdk;

import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.VoiceInTrunkGroup;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * A 429 of the platform API, and the Retry-After of any error status, on every
 * path that can receive one: the JSON:API reads and writes, the export
 * downloads and the encrypted file upload.
 */
class TooManyRequestsTest extends BaseTest {
    private static final String TOO_MANY_REQUESTS_BODY = "{\"errors\":[{\"title\":\"Too Many Requests\","
            + "\"detail\":\"The request budget of the account is spent\",\"code\":\"too_many_requests\",\"status\":\"429\"}]}";

    @TempDir
    Path dir;

    @FunctionalInterface
    interface Operation {
        void run(DidwwClient client, String origin, Path dir);
    }

    static Stream<Named<Operation>> operations() {
        return Stream.of(
                Named.of("GET a JSON:API list", (c, o, d) -> c.dids().list()),
                Named.of("POST a JSON:API resource", (c, o, d) -> {
                    VoiceInTrunkGroup group = new VoiceInTrunkGroup();
                    group.setName("group");
                    c.voiceInTrunkGroups().create(group);
                }),
                Named.of("PATCH a JSON:API resource", (c, o, d) -> {
                    Did did = new Did().withId("did-id");
                    did.setDescription("updated");
                    c.dids().update(did);
                }),
                Named.of("DELETE a JSON:API resource", (c, o, d) -> c.voiceInTrunkGroups().delete("group-id")),
                Named.of("download an export", (c, o, d) ->
                        c.downloadExport(o + "/v3/exports/e.csv.gz", OutputStream.nullOutputStream())),
                Named.of("download and decompress an export to a path", (c, o, d) ->
                        c.downloadAndDecompressExport(o + "/v3/exports/e.csv.gz", d.resolve("export.csv"))),
                Named.of("upload an encrypted file", (c, o, d) ->
                        c.uploadEncryptedFile("x".getBytes(StandardCharsets.UTF_8), "f.enc", "fingerprint", null)));
    }

    static Stream<Arguments> invalidRetryAfter() {
        List<String> values = Arrays.asList("1.5", "Wed, 21 Oct 2026 07:28:00 GMT", "-1", "", "1000000000",
                "99999999999999999999");
        return operations().flatMap(operation -> values.stream().map(value -> Arguments.of(operation, value)));
    }

    private void answerEveryApiCall(ResponseDefinitionBuilder response) {
        wireMock.stubFor(any(urlPathMatching("/v3/.*")).willReturn(response));
    }

    private DidwwApiException failure(Operation operation) {
        String origin = origin();
        DidwwApiException e = catchThrowableOfType(() -> operation.run(client, origin, dir), DidwwApiException.class);
        assertThat(e).as("a DidwwApiException").isNotNull();
        return e;
    }

    private int apiRequests() {
        return wireMock.findAll(anyRequestedFor(urlPathMatching("/v3/.*"))).size();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void testTooManyRequestsCarriesRetryAfterAndIsNotRetried(Operation operation) {
        answerEveryApiCall(aResponse().withStatus(429)
                .withHeader("Content-Type", "application/vnd.api+json")
                .withHeader("Retry-After", "30")
                .withBody(TOO_MANY_REQUESTS_BODY));

        DidwwApiException e = failure(operation);

        assertThat(e.getHttpStatus()).isEqualTo(429);
        assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(30));
        assertThat(e.getErrors()).extracting(DidwwApiException.ApiError::getCode).containsExactly("too_many_requests");
        assertThat(apiRequests()).as("a 429 is not retried").isEqualTo(1);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void testTooManyRequestsWithoutAnErrorsDocument(Operation operation) {
        answerEveryApiCall(aResponse().withStatus(429)
                .withHeader("Content-Type", "text/plain")
                .withHeader("Retry-After", "2")
                .withBody("Too Many Requests"));

        DidwwApiException e = failure(operation);

        assertThat(e.getHttpStatus()).isEqualTo(429);
        assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(2));
        assertThat(apiRequests()).isEqualTo(1);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void testRetryAfterOfAnotherErrorStatus(Operation operation) {
        answerEveryApiCall(aResponse().withStatus(503)
                .withHeader("Content-Type", "application/vnd.api+json")
                .withHeader("Retry-After", "7")
                .withBody("{\"errors\":[{\"title\":\"Service Unavailable\",\"status\":\"503\"}]}"));

        DidwwApiException e = failure(operation);

        assertThat(e.getHttpStatus()).isEqualTo(503);
        assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(7));
    }

    @ParameterizedTest(name = "{0}: Retry-After \"{1}\"")
    @MethodSource("invalidRetryAfter")
    void testRetryAfterThatIsNotSecondsReadsAsNull(Operation operation, String retryAfter) {
        answerEveryApiCall(aResponse().withStatus(429)
                .withHeader("Content-Type", "application/vnd.api+json")
                .withHeader("Retry-After", retryAfter)
                .withBody(TOO_MANY_REQUESTS_BODY));

        DidwwApiException e = failure(operation);

        assertThat(e.getHttpStatus()).isEqualTo(429);
        assertThat(e.getRetryAfter()).isNull();
    }

    @Test
    void testLargestRetryAfterIsRead() {
        answerEveryApiCall(aResponse().withStatus(429)
                .withHeader("Content-Type", "application/vnd.api+json")
                .withHeader("Retry-After", "999999999")
                .withBody(TOO_MANY_REQUESTS_BODY));

        DidwwApiException e = failure((c, o, d) -> c.dids().list());

        assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(999999999));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void testErrorWithoutRetryAfter(Operation operation) {
        answerEveryApiCall(aResponse().withStatus(422)
                .withHeader("Content-Type", "application/vnd.api+json")
                .withBody("{\"errors\":[{\"title\":\"is invalid\",\"status\":\"422\"}]}"));

        DidwwApiException e = failure(operation);

        assertThat(e.getHttpStatus()).isEqualTo(422);
        assertThat(e.getRetryAfter()).isNull();
    }
}
