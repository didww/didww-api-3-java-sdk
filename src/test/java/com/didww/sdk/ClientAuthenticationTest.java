package com.didww.sdk;

import com.didww.sdk.auth.AccessToken;
import com.didww.sdk.auth.DidwwScope;
import com.didww.sdk.auth.RecordingLogger;
import com.didww.sdk.auth.TokenStore;
import com.didww.sdk.exception.AuthErrorKind;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwAuthException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.exception.DidwwException;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ClientAuthenticationTest extends BaseTest {

    private void stubTokens(String first, String second) {
        wireMock.stubFor(post(urlEqualTo("/token"))
                .inScenario("tokens").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(okJson(tokenJson(first, API_AUDIENCE)))
                .willSetStateTo("second"));
        wireMock.stubFor(post(urlEqualTo("/token"))
                .inScenario("tokens").whenScenarioStateIs("second")
                .willReturn(okJson(tokenJson(second, API_AUDIENCE))));
    }

    @Test
    void testUserAgentHeaderIsSent() {
        stubGetFixture("/v3/balance", "balance/index.json");

        client.balance().find();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/balance"))
                .withHeader("User-Agent", equalTo(SdkVersion.userAgent())));

        assertThat(SdkVersion.get()).isNotEqualTo("unknown");
        assertThat(SdkVersion.userAgent()).isEqualTo("didww-java-sdk/" + SdkVersion.get());
    }

    @Test
    void testTokenReusedAcrossRequests() {
        stubGetFixture("/v3/balance", "balance/index.json");

        client.balance().find();
        client.balance().find();

        assertThat(mints()).isEqualTo(1);
        wireMock.verify(2, getRequestedFor(urlPathEqualTo("/v3/balance")).withoutHeader("Api-Key"));
    }

    @Test
    void testAuthErrorSurfacesFromRepositories() {
        wireMock.stubFor(post(urlEqualTo("/token")).atPriority(1)
                .willReturn(aResponse().withStatus(401).withBody(loadFixture("token/invalid_client.json"))));

        DidwwAuthException e = catchThrowableOfType(() -> client.balance().find(), DidwwAuthException.class);

        assertThat(e).isNotNull().isInstanceOf(DidwwException.class);
        assertThat(e.getKind()).isEqualTo(AuthErrorKind.INVALID_CLIENT);
        assertThat(e.getMessage()).doesNotContain(TEST_API_KEY);
        wireMock.verify(0, getRequestedFor(urlPathEqualTo("/v3/balance")));
    }

    @Test
    void testTransportErrorsAreWrapped() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            port = socket.getLocalPort();
        }
        DidwwClient dead = clientBuilder().baseUrl("http://127.0.0.1:" + port + "/v3").build();

        DidwwClientException e = catchThrowableOfType(() -> dead.balance().find(), DidwwClientException.class);

        assertThat(e).isNotNull();
        assertThat(e.getCause()).isInstanceOf(IOException.class);
    }

    @Test
    void testPersistent401IsReturnedAfterOneRetry() {
        stubTokens("token-1", "token-2");
        wireMock.stubFor(get(urlPathEqualTo("/v3/balance")).willReturn(aResponse().withStatus(401)
                .withBody("{\"errors\":[{\"title\":\"Unauthorized\",\"status\":\"401\"}]}")));

        DidwwApiException e = catchThrowableOfType(() -> client.balance().find(), DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(401);
        assertThat(mints()).isEqualTo(2);
        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/v3/balance")).withHeader("Authorization", equalTo("Bearer token-1")));
        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/v3/balance")).withHeader("Authorization", equalTo("Bearer token-2")));
    }

    @Test
    void testForbiddenIsNotRetried() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/balance")).willReturn(aResponse().withStatus(403)
                .withBody("{\"errors\":[{\"title\":\"insufficient_scope\",\"status\":\"403\"}]}")));

        DidwwApiException e = catchThrowableOfType(() -> client.balance().find(), DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(403);
        assertThat(mints()).isEqualTo(1);
        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/v3/balance")));
    }

    @Test
    void testUserAuthenticatorDoesNotSwallow401() {
        stubTokens("token-1", "token-2");
        wireMock.stubFor(get(urlPathEqualTo("/v3/balance")).willReturn(aResponse().withStatus(401)));
        AtomicBoolean authenticatorCalled = new AtomicBoolean();
        DidwwClient custom = clientBuilder()
                .httpClientBuilder(new OkHttpClient.Builder().authenticator((route, response) -> {
                    authenticatorCalled.set(true);
                    return response.request().newBuilder().header("Authorization", "Bearer from-authenticator").build();
                }))
                .build();

        DidwwApiException e = catchThrowableOfType(() -> custom.balance().find(), DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(401);
        assertThat(authenticatorCalled).isFalse();
        assertThat(mints()).isEqualTo(2);
        wireMock.verify(2, getRequestedFor(urlPathEqualTo("/v3/balance")));
    }

    @Test
    void testTokenRequestRedirectIsNotFollowed() {
        wireMock.stubFor(post(urlEqualTo("/token")).atPriority(1)
                .willReturn(aResponse().withStatus(307).withHeader("Location", origin() + "/elsewhere")));
        wireMock.stubFor(post(urlEqualTo("/elsewhere")).willReturn(okJson(tokenJson(TEST_ACCESS_TOKEN, API_AUDIENCE))));

        DidwwAuthException e = catchThrowableOfType(() -> client.balance().find(), DidwwAuthException.class);

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.UNEXPECTED_STATUS);
        assertThat(e.getHttpStatus()).isEqualTo(307);
        wireMock.verify(0, postRequestedFor(urlEqualTo("/elsewhere")));
    }

    @Test
    void testApiRedirectIsNotFollowed() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/balance")).willReturn(aResponse().withStatus(302)
                .withHeader("Location", origin() + "/elsewhere")));
        wireMock.stubFor(get(urlEqualTo("/elsewhere")).willReturn(okJson("{}")));

        DidwwApiException e = catchThrowableOfType(() -> client.balance().find(), DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(302);
        wireMock.verify(0, getRequestedFor(urlEqualTo("/elsewhere")));
    }

    @Test
    void testRefusedTokenKeptByStoreIsNotSentAgain() {
        stubTokens("token-1", "token-2");
        wireMock.stubFor(get(urlPathEqualTo("/v3/balance")).withHeader("Authorization", equalTo("Bearer token-1"))
                .willReturn(aResponse().withStatus(401)));
        wireMock.stubFor(get(urlPathEqualTo("/v3/balance")).withHeader("Authorization", equalTo("Bearer token-2"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("balance/index.json"))));
        ConcurrentHashMap<String, AccessToken> tokens = new ConcurrentHashMap<>();
        TokenStore keeping = new TokenStore() {
            @Override
            public AccessToken get(String key) {
                return tokens.get(key);
            }

            @Override
            public void set(String key, AccessToken token, Duration ttl) {
                tokens.put(key, token);
            }

            @Override
            public void delete(String key) {
                // Keeps the refused token.
            }
        };
        DidwwClient custom = clientBuilder().tokenStore(keeping).build();

        custom.balance().find();
        custom.balance().find();

        assertThat(mints()).isEqualTo(2);
        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/v3/balance")).withHeader("Authorization", equalTo("Bearer token-1")));
        wireMock.verify(2, getRequestedFor(urlPathEqualTo("/v3/balance")).withHeader("Authorization", equalTo("Bearer token-2")));
    }

    @Test
    void testScopeIsRequested() {
        wireMock.stubFor(post(urlEqualTo("/token")).atPriority(1)
                .withRequestBody(equalToJson(loadFixture("token/create_request_read_scope.json"), false, false))
                .willReturn(okJson(tokenJson(TEST_ACCESS_TOKEN, API_AUDIENCE).replace("\"full\"", "\"read\""))));
        stubGetFixture("/v3/balance", "balance/index.json");
        DidwwClient readOnly = clientBuilder().scope(com.didww.sdk.auth.DidwwScope.READ).build();

        readOnly.balance().find();

        assertThat(mints()).isEqualTo(1);
        wireMock.verify(1, postRequestedFor(urlEqualTo("/token"))
                .withRequestBody(equalToJson(loadFixture("token/create_request_read_scope.json"), false, false)));
    }

    @Test
    void testApiVersionIsRequested() {
        wireMock.stubFor(post(urlEqualTo("/token")).atPriority(1)
                .willReturn(okJson(tokenJson(TEST_ACCESS_TOKEN, API_AUDIENCE).replace("2026-10-22", "2099-01-01"))));
        stubGetFixture("/v3/balance", "balance/index.json");
        DidwwClient later = clientBuilder().apiVersion("2099-01-01").build();

        later.balance().find();

        wireMock.verify(1, postRequestedFor(urlEqualTo("/token"))
                .withRequestBody(matchingJsonPath("$.api_version", equalTo("2099-01-01"))));
    }

    @Test
    void testLoggerReceivesTokenWarnings() {
        stubGetFixture("/v3/balance", "balance/index.json");
        RecordingLogger logger = new RecordingLogger();
        DidwwClient readOnly = clientBuilder().scope(DidwwScope.READ).logger(logger).build();

        readOnly.balance().find();

        assertThat(logger.messages(System.Logger.Level.WARNING))
                .containsExactly("DIDWW token scope mismatch: requested read, granted full");
    }
}
