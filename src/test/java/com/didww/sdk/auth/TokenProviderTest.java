package com.didww.sdk.auth;

import com.didww.sdk.SdkVersion;
import com.didww.sdk.exception.AuthErrorKind;
import com.didww.sdk.exception.DidwwAuthException;
import com.didww.sdk.internal.Jitter;
import com.didww.sdk.support.RawHttpServer;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.ScenarioMappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.OkHttpClient;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class TokenProviderTest {
    private static final String API = "api.didww.com";
    private static final String SMS = "api-sms.didww.com";
    private static final String API_KEY = "test-api-key";
    private static final Instant MINTED_AT = Instant.parse("2026-10-22T08:00:00Z");
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(Duration.ofSeconds(2))
            .readTimeout(Duration.ofSeconds(5))
            .build();

    private WireMockServer wireMock;
    private final List<Duration> sleeps = Collections.synchronizedList(new ArrayList<>());
    private final RecordingLogger logger = new RecordingLogger();

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().bindAddress("127.0.0.1"));
        wireMock.start();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
        TokenProvider.forgetRateLimits();
        Thread.interrupted();
    }

    private String origin() {
        return "http://127.0.0.1:" + wireMock.port();
    }

    private TokenProvider provider() {
        return provider(origin(), API_KEY, null, "2026-10-22", new MemoryTokenStore());
    }

    private TokenProvider provider(TokenStore store) {
        return provider(origin(), API_KEY, null, "2026-10-22", store);
    }

    private TokenProvider provider(String authUrl, String apiKey, DidwwScope scope, String version, TokenStore store) {
        TokenProvider provider = new TokenProvider(HTTP, authUrl, apiKey, version, scope, store, logger);
        provider.setSleeper(sleeps::add);
        provider.setRandom(() -> 1.0);
        provider.setClock(Clock.fixed(MINTED_AT, ZoneOffset.UTC));
        return provider;
    }

    private String fixture(String path) {
        try {
            return new String(getClass().getClassLoader().getResourceAsStream("fixtures/" + path).readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String tokenJson(String accessToken, String audience) {
        return "{\"access_token\":\"" + accessToken + "\",\"token_type\":\"Bearer\",\"expires_in\":900,"
                + "\"aud\":[\"" + audience + "\"],\"scope\":\"full\",\"api_version\":\"2026-10-22\"}";
    }

    private void stubToken(ResponseDefinitionBuilder response) {
        wireMock.stubFor(post(urlEqualTo("/token")).willReturn(response));
    }

    private void stubTokenSequence(ResponseDefinitionBuilder... responses) {
        for (int i = 0; i < responses.length; i++) {
            ScenarioMappingBuilder mapping = post(urlEqualTo("/token"))
                    .inScenario("mint")
                    .whenScenarioStateIs(i == 0 ? Scenario.STARTED : "s" + i)
                    .willReturn(responses[i]);
            if (i + 1 < responses.length) {
                mapping = mapping.willSetStateTo("s" + (i + 1));
            }
            wireMock.stubFor(mapping);
        }
    }

    private int mints() {
        return wireMock.findAll(postRequestedFor(urlEqualTo("/token"))).size();
    }

    private static DidwwAuthException authError(ThrowingCallable call) {
        DidwwAuthException e = catchThrowableOfType(call, DidwwAuthException.class);
        assertThat(e).as("DidwwAuthException").isNotNull();
        return e;
    }

    private static String chain(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (Throwable t = e; t != null; t = t.getCause()) {
            sb.append(t).append('\n');
        }
        return sb.toString();
    }

    // --- mint request ---

    @Test
    void testMintRequest() {
        stubToken(okJson(fixture("token/create.json")));

        AccessToken token = provider().token(API);

        assertThat(token.getAccessToken()).isEqualTo("test-access-token");
        assertThat(token.getTokenType()).isEqualTo("Bearer");
        assertThat(token.getExpiresIn()).isEqualTo(900);
        assertThat(token.getAudience()).containsExactly(API);
        assertThat(token.getScope()).isEqualTo("full");
        assertThat(token.getApiVersion()).isEqualTo("2026-10-22");
        assertThat(token.getMintedAt()).isEqualTo(MINTED_AT);
        wireMock.verify(1, postRequestedFor(urlEqualTo("/token"))
                .withHeader("Api-Key", equalTo(API_KEY))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("User-Agent", equalTo(SdkVersion.userAgent()))
                .withoutHeader("Authorization")
                .withRequestBody(equalToJson(fixture("token/create_request.json"), false, false)));
        assertThat(logger.entries()).isEmpty();
    }

    @Test
    void testMintRequestWithScope() {
        stubToken(okJson(tokenJson("t", API).replace("\"full\"", "\"read\"")));

        provider(origin(), API_KEY, DidwwScope.READ, "2026-10-22", new MemoryTokenStore()).token(API);

        wireMock.verify(postRequestedFor(urlEqualTo("/token"))
                .withRequestBody(equalToJson(fixture("token/create_request_read_scope.json"), false, false)));
    }

    @Test
    void testMintRequestWithApiVersion() {
        stubToken(okJson(tokenJson("t", API).replace("2026-10-22", "2099-01-01")));

        provider(origin(), API_KEY, null, "2099-01-01", new MemoryTokenStore()).token(API);

        wireMock.verify(postRequestedFor(urlEqualTo("/token"))
                .withRequestBody(equalToJson("{\"aud\":[\"api.didww.com\"],\"api_version\":\"2099-01-01\"}", false, false)));
    }

    // --- caching and single flight ---

    @Test
    void testTokenReused() {
        stubToken(okJson(tokenJson("t", API)));
        TokenProvider provider = provider();

        provider.token(API);
        provider.token(API);

        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testTokenMintedPerAudience() {
        wireMock.stubFor(post(urlEqualTo("/token")).withRequestBody(matchingJsonPath("$.aud[0]", equalTo(API)))
                .willReturn(okJson(tokenJson("api-token", API))));
        wireMock.stubFor(post(urlEqualTo("/token")).withRequestBody(matchingJsonPath("$.aud[0]", equalTo(SMS)))
                .willReturn(okJson(tokenJson("sms-token", SMS))));
        TokenProvider provider = provider();

        assertThat(provider.token(API).getAccessToken()).isEqualTo("api-token");
        assertThat(provider.token(SMS).getAccessToken()).isEqualTo("sms-token");
        assertThat(provider.token(API).getAccessToken()).isEqualTo("api-token");
        assertThat(mints()).isEqualTo(2);
    }

    @Test
    void testTokenSharedThroughStore() {
        stubToken(okJson(tokenJson("t", API)));
        MemoryTokenStore store = new MemoryTokenStore();

        provider(store).token(API);
        provider(store).token(API);

        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testTokenStoredForGrantedLifetime() {
        stubToken(okJson(tokenJson("t", API).replace("900", "300")));
        TestStores.Recording store = new TestStores.Recording();

        provider(store).token(API);

        assertThat(store.keys).hasSize(1);
        assertThat(store.keys.get(0)).startsWith("didww:token:v1:");
        assertThat(store.ttls).containsExactly(Duration.ofSeconds(300));
    }

    @Test
    void testCacheKey() {
        String base = provider("https://auth.example.com", "key-1", null, "2026-10-22", new MemoryTokenStore()).cacheKey(API);

        assertThat(base).isEqualTo("didww:token:v1:2e1c5435f0e67f91d34497fabf69840a945923a47982fad172c353b3f26be539");
        assertThat(base).doesNotContain("key-1");
        assertThat(provider("https://auth.example.com/", "key-1", null, "2026-10-22", new MemoryTokenStore()).cacheKey(API))
                .isEqualTo(base);
        assertThat(provider("https://auth.example.com", "key-1", null, "2026-10-22", new MemoryTokenStore()).cacheKey(SMS))
                .isNotEqualTo(base);
        assertThat(provider("https://auth.example.com", "key-1", null, "2099-01-01", new MemoryTokenStore()).cacheKey(API))
                .isNotEqualTo(base);
        assertThat(provider("https://auth.example.com", "key-1", DidwwScope.READ, "2026-10-22", new MemoryTokenStore()).cacheKey(API))
                .isNotEqualTo(base);
        assertThat(provider("https://auth-sandbox.example.com", "key-1", null, "2026-10-22", new MemoryTokenStore()).cacheKey(API))
                .isNotEqualTo(base);
        assertThat(provider("https://auth.example.com", "key-2", null, "2026-10-22", new MemoryTokenStore()).cacheKey(API))
                .isNotEqualTo(base);
    }

    @Test
    void testSingleFlight() throws Exception {
        stubToken(okJson(tokenJson("shared-token", API)).withFixedDelay(300));
        TokenProvider provider = provider();
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<AccessToken>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return provider.token(API);
                }));
            }
            start.countDown();
            for (Future<AccessToken> result : results) {
                assertThat(result.get(10, TimeUnit.SECONDS).getAccessToken()).isEqualTo("shared-token");
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testWaiterInterruptDoesNotCancelMint() throws Exception {
        stubToken(okJson(tokenJson("t", API)).withFixedDelay(1000));
        CountDownLatch leaderMinting = new CountDownLatch(1);
        CountDownLatch waiterWaiting = new CountDownLatch(1);
        TestStores.Hooked store = new TestStores.Hooked(s -> {
            if (s.gets == 2) {
                leaderMinting.countDown();
            } else if (s.gets == 3) {
                waiterWaiting.countDown();
            }
        });
        TokenProvider provider = provider(store);
        AtomicReference<AccessToken> leaderToken = new AtomicReference<>();
        AtomicReference<DidwwAuthException> waiterError = new AtomicReference<>();
        AtomicBoolean waiterInterrupted = new AtomicBoolean();

        Thread leader = new Thread(() -> leaderToken.set(provider.token(API)));
        leader.start();
        assertThat(leaderMinting.await(5, TimeUnit.SECONDS)).isTrue();
        Thread waiter = new Thread(() -> {
            try {
                provider.token(API);
            } catch (DidwwAuthException e) {
                waiterError.set(e);
                waiterInterrupted.set(Thread.currentThread().isInterrupted());
            }
        });
        waiter.start();
        assertThat(waiterWaiting.await(5, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(100);
        waiter.interrupt();
        waiter.join(5000);
        leader.join(5000);

        assertThat(waiterError.get()).isNotNull();
        assertThat(waiterError.get().getKind()).isEqualTo(AuthErrorKind.CONNECTION_ERROR);
        assertThat(waiterInterrupted.get()).isTrue();
        assertThat(leaderToken.get().getAccessToken()).isEqualTo("t");
        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testFlightRereadsStoreBeforeMinting() {
        AccessToken stored = new AccessToken("from-another-process", "Bearer", 900,
                Collections.singletonList(API), "full", "2026-10-22", MINTED_AT);
        AtomicReference<TokenProvider> ref = new AtomicReference<>();
        TestStores.Hooked store = new TestStores.Hooked(s -> {
            if (s.gets == 2) {
                s.tokens.put(ref.get().cacheKey(API), stored);
            }
        });
        ref.set(provider(store));

        AccessToken token = ref.get().token(API);

        assertThat(token.getAccessToken()).isEqualTo("from-another-process");
        assertThat(mints()).isZero();
    }

    @Test
    void testLaterCallMintsAgainAfterFailure() {
        stubTokenSequence(aResponse().withStatus(401).withBody(fixture("token/invalid_client.json")),
                okJson(tokenJson("t", API)));
        TokenProvider provider = provider();

        assertThat(authError(() -> provider.token(API)).getKind()).isEqualTo(AuthErrorKind.INVALID_CLIENT);
        assertThat(provider.token(API).getAccessToken()).isEqualTo("t");
        assertThat(mints()).isEqualTo(2);
    }

    // --- store behaviour ---

    @Test
    void testStoreReadFailureIsLoggedNotFatal() {
        stubToken(okJson(tokenJson("t", API)));

        AccessToken token = provider(new TestStores.Failing(true, false, false)).token(API);

        assertThat(token.getAccessToken()).isEqualTo("t");
        assertThat(logger.messages(System.Logger.Level.WARNING)).contains("DIDWW token store read failed");
        assertThat(logger.entries().get(0).thrown).hasMessage("store get failed");
    }

    @Test
    void testStoreWriteFailureIsLoggedNotFatal() {
        stubToken(okJson(tokenJson("t", API)));

        AccessToken token = provider(new TestStores.Failing(false, true, false)).token(API);

        assertThat(token.getAccessToken()).isEqualTo("t");
        assertThat(logger.messages(System.Logger.Level.WARNING)).containsExactly("DIDWW token store write failed");
    }

    @Test
    void testStoreDeleteFailureIsLoggedNotFatal() {
        stubTokenSequence(okJson(tokenJson("token-1", API)), okJson(tokenJson("token-2", API)));
        TestStores.Failing store = new TestStores.Failing(false, false, true);
        TokenProvider provider = provider(store);

        provider.token(API);
        assertThatCode(() -> provider.invalidate(API, "token-1")).doesNotThrowAnyException();

        assertThat(logger.messages(System.Logger.Level.WARNING)).containsExactly("DIDWW token store delete failed");
        assertThat(provider.token(API).getAccessToken()).isEqualTo("token-2");
    }

    @Test
    void testStalledStoreIsBounded() {
        stubToken(okJson(tokenJson("t", API)));
        TestStores.Stalling store = new TestStores.Stalling();
        TokenProvider provider = provider(store);
        provider.setStoreTimeout(Duration.ofMillis(100));
        long start = System.nanoTime();
        try {
            AccessToken token = provider.token(API);

            assertThat(token.getAccessToken()).isEqualTo("t");
            assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(3));
            assertThat(logger.messages(System.Logger.Level.WARNING))
                    .containsExactly("DIDWW token store read failed", "DIDWW token store read failed");
        } finally {
            store.release.countDown();
        }
    }

    @Test
    void testStoreWriteSkippedWhileTheStoreLockIsBusy() throws Exception {
        stubToken(okJson(tokenJson("t", API)));
        MemoryTokenStore store = new MemoryTokenStore();
        TokenProvider provider = provider(store);
        provider.setStoreTimeout(Duration.ofMillis(100));
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread holder = new Thread(() -> {
            provider.storeLock(provider.cacheKey(API)).lock();
            locked.countDown();
            try {
                release.await();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
        holder.start();
        try {
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();

            AccessToken token = provider.token(API);

            assertThat(token.getAccessToken()).isEqualTo("t");
            assertThat(store.get(provider.cacheKey(API))).isNull();
            assertThat(logger.messages(System.Logger.Level.WARNING)).containsExactly("DIDWW token store write skipped");
        } finally {
            release.countDown();
            holder.join(5000);
        }
    }

    @Test
    void testRefusedTokenKeptByStoreIsNotReused() {
        stubTokenSequence(okJson(tokenJson("token-1", API)), okJson(tokenJson("token-2", API)));
        TestStores.KeepingRefused store = new TestStores.KeepingRefused();
        TokenProvider provider = provider(store);

        assertThat(provider.token(API).getAccessToken()).isEqualTo("token-1");
        provider.invalidate(API, "token-1");
        assertThat(store.get(provider.cacheKey(API)).getAccessToken()).isEqualTo("token-1");

        assertThat(provider.token(API).getAccessToken()).isEqualTo("token-2");
        assertThat(provider.token(API).getAccessToken()).isEqualTo("token-2");
        assertThat(mints()).isEqualTo(2);
        assertThat(store.deletes).isEqualTo(1);
    }

    @Test
    void testInvalidateEvictsTheRefusedToken() {
        stubTokenSequence(okJson(tokenJson("token-1", API)), okJson(tokenJson("token-2", API)));
        MemoryTokenStore store = new MemoryTokenStore();
        TokenProvider provider = provider(store);

        provider.token(API);
        provider.invalidate(API, "token-1");

        assertThat(store.get(provider.cacheKey(API))).isNull();
        assertThat(provider.token(API).getAccessToken()).isEqualTo("token-2");
    }

    @Test
    void testInvalidateKeepsTokenMintedByAnotherCaller() {
        TestStores.Recording store = new TestStores.Recording();
        TokenProvider provider = provider(store);
        store.tokens.put(provider.cacheKey(API), new AccessToken("token-2", "Bearer", 900,
                Collections.singletonList(API), "full", "2026-10-22", MINTED_AT));

        provider.invalidate(API, "token-1");

        assertThat(store.deletes).isZero();
        assertThat(provider.token(API).getAccessToken()).isEqualTo("token-2");
        assertThat(mints()).isZero();
    }

    // --- error statuses ---

    @Test
    void testInvalidClient() {
        stubToken(aResponse().withStatus(401).withHeader("Content-Type", "application/json")
                .withBody(fixture("token/invalid_client.json")));

        DidwwAuthException e = authError(() -> provider().token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.INVALID_CLIENT);
        assertThat(e.getHttpStatus()).isEqualTo(401);
        assertThat(e.getError()).isEqualTo("invalid_client");
        assertThat(e.getErrorDescription()).isEqualTo("Client authentication failed.");
        assertThat(e).hasMessage("Token service: invalid_client: Client authentication failed.");
        assertThat(e.getMessage()).doesNotContain(API_KEY);
        assertThat(mints()).isEqualTo(1);
        assertThat(sleeps).isEmpty();
    }

    @Test
    void testInvalidRequest() {
        stubToken(aResponse().withStatus(400).withBody(fixture("token/invalid_scope.json")));

        DidwwAuthException e = authError(() -> provider().token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.INVALID_REQUEST);
        assertThat(e.getError()).isEqualTo("invalid_scope");
        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testRateLimited() {
        stubToken(aResponse().withStatus(429).withHeader("Retry-After", "2")
                .withBody(fixture("token/rate_limit_exceeded.json")));
        AtomicLong now = new AtomicLong(1_000_000_000L);
        TokenProvider provider = provider();
        provider.setNanoTime(now::get);
        provider.setRandom(() -> 0.0);

        DidwwAuthException first = authError(() -> provider.token(API));
        assertThat(first.getKind()).isEqualTo(AuthErrorKind.RATE_LIMITED);
        assertThat(first.getHttpStatus()).isEqualTo(429);
        assertThat(first.getError()).isEqualTo("rate_limit_exceeded");
        assertThat(first.getRetryAfter()).isEqualTo(Duration.ofSeconds(2));

        now.addAndGet(Duration.ofMillis(500).toNanos());
        DidwwAuthException second = authError(() -> provider.token(API));
        assertThat(second.getKind()).isEqualTo(AuthErrorKind.RATE_LIMITED);
        assertThat(second.getRetryAfter()).isEqualTo(Duration.ofSeconds(2));
        assertThat(mints()).isEqualTo(1);

        now.addAndGet(Duration.ofMillis(1000).toNanos());
        assertThat(authError(() -> provider.token(API)).getRetryAfter()).isEqualTo(Duration.ofSeconds(1));
        assertThat(mints()).isEqualTo(1);

        now.addAndGet(Duration.ofMillis(600).toNanos());
        authError(() -> provider.token(API));
        assertThat(mints()).isEqualTo(2);
    }

    @Test
    void testRateLimitWindowIsSpreadWithJitter() {
        stubToken(aResponse().withStatus(429).withHeader("Retry-After", "2"));
        AtomicLong now = new AtomicLong();
        TokenProvider provider = provider();
        provider.setNanoTime(now::get);
        provider.setRandom(() -> 1.0);

        authError(() -> provider.token(API));
        now.addAndGet(Duration.ofMillis(2900).toNanos());
        assertThat(authError(() -> provider.token(API)).getRetryAfter()).isEqualTo(Duration.ofSeconds(1));
        assertThat(mints()).isEqualTo(1);

        now.addAndGet(Duration.ofMillis(200).toNanos());
        authError(() -> provider.token(API));
        assertThat(mints()).isEqualTo(2);
    }

    @Test
    void testRateLimitWindowSharedByProvidersOfTheSameKey() {
        stubToken(aResponse().withStatus(429).withHeader("Retry-After", "30"));

        authError(() -> provider().token(API));
        DidwwAuthException other = authError(() -> provider(new MemoryTokenStore()).token(SMS));

        assertThat(other.getKind()).isEqualTo(AuthErrorKind.RATE_LIMITED);
        assertThat(mints()).isEqualTo(1);

        stubToken(okJson(tokenJson("t", API)));
        assertThat(provider(origin(), "other-api-key", null, "2026-10-22", new MemoryTokenStore()).token(API)
                .getAccessToken()).isEqualTo("t");
        assertThat(mints()).isEqualTo(2);
    }

    @Test
    void testRateLimitedWithZeroRetryAfterStillOpensAWindow() {
        stubToken(aResponse().withStatus(429).withHeader("Retry-After", "0"));
        TokenProvider provider = provider();

        assertThat(authError(() -> provider.token(API)).getRetryAfter()).isEqualTo(Duration.ofSeconds(1));
        assertThat(authError(() -> provider.token(API)).getKind()).isEqualTo(AuthErrorKind.RATE_LIMITED);
        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testRetryAfterParsing() {
        assertThat(TokenProvider.retryAfter(null)).isEqualTo(Duration.ofSeconds(1));
        assertThat(TokenProvider.retryAfter("0")).isEqualTo(Duration.ofSeconds(1));
        assertThat(TokenProvider.retryAfter("5")).isEqualTo(Duration.ofSeconds(5));
        assertThat(TokenProvider.retryAfter(" 7 ")).isEqualTo(Duration.ofSeconds(7));
        assertThat(TokenProvider.retryAfter("+5")).isEqualTo(Duration.ofSeconds(1));
        assertThat(TokenProvider.retryAfter("-5")).isEqualTo(Duration.ofSeconds(1));
        assertThat(TokenProvider.retryAfter("1.5")).isEqualTo(Duration.ofSeconds(1));
        assertThat(TokenProvider.retryAfter("12345678901")).isEqualTo(Duration.ofSeconds(1));
        assertThat(TokenProvider.retryAfter("Thu, 22 Oct 2026 08:05:03 GMT")).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    void testServerErrorRetriedWithBackoff() {
        stubTokenSequence(
                aResponse().withStatus(503).withBody(fixture("token/server_error.json")),
                aResponse().withStatus(503),
                aResponse().withStatus(502),
                okJson(tokenJson("token-ok", API)));

        AccessToken token = provider().token(API);

        assertThat(token.getAccessToken()).isEqualTo("token-ok");
        assertThat(mints()).isEqualTo(4);
        assertThat(sleeps).containsExactly(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(4));
    }

    @Test
    void testServerErrorBackoffUsesFullJitter() {
        stubTokenSequence(aResponse().withStatus(500), aResponse().withStatus(500), okJson(tokenJson("t", API)));
        TokenProvider provider = provider();
        provider.setRandom(() -> 0.5);

        provider.token(API);

        assertThat(sleeps).containsExactly(Duration.ofMillis(500), Duration.ofSeconds(1));
    }

    @Test
    void testBackoffCap() {
        TokenProvider provider = provider();

        assertThat(provider.backoff(1)).isEqualTo(Duration.ofSeconds(1));
        assertThat(provider.backoff(3)).isEqualTo(Duration.ofSeconds(4));
        assertThat(provider.backoff(4)).isEqualTo(Duration.ofSeconds(8));
        assertThat(provider.backoff(5)).isEqualTo(Duration.ofSeconds(8));
        assertThat(provider.backoff(40)).isEqualTo(Duration.ofSeconds(8));
    }

    @Test
    void testSpreadRetryAfter() {
        assertThat(Jitter.spread(Duration.ofSeconds(1), () -> 0)).isEqualTo(Duration.ofSeconds(1));
        assertThat(Jitter.spread(Duration.ofSeconds(1), () -> 1)).isEqualTo(Duration.ofSeconds(2));
        assertThat(Jitter.spread(Duration.ofSeconds(30), () -> 0)).isEqualTo(Duration.ofSeconds(30));
        assertThat(Jitter.spread(Duration.ofSeconds(30), () -> 1)).isEqualTo(Duration.ofSeconds(36));
        assertThat(Jitter.spread(Duration.ofSeconds(30), () -> 0.5)).isEqualTo(Duration.ofSeconds(33));
    }

    @Test
    void testServerErrorExhausted() {
        stubToken(aResponse().withStatus(500).withBody(fixture("token/server_error.json")));

        DidwwAuthException e = authError(() -> provider().token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.SERVER_ERROR);
        assertThat(e.getHttpStatus()).isEqualTo(500);
        assertThat(e.getError()).isEqualTo("server_error");
        assertThat(mints()).isEqualTo(TokenProvider.MAX_MINT_ATTEMPTS);
        assertThat(sleeps).hasSize(TokenProvider.MAX_MINT_ATTEMPTS - 1);
    }

    @Test
    void testServerErrorWithoutJsonBody() {
        stubToken(aResponse().withStatus(502).withBody("<html>Bad Gateway</html>"));

        DidwwAuthException e = authError(() -> provider().token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.SERVER_ERROR);
        assertThat(e.getHttpStatus()).isEqualTo(502);
        assertThat(e.getError()).isNull();
        assertThat(e).hasMessage("Token service responded with status 502");
    }

    @Test
    void testUnexpectedStatus() {
        stubToken(aResponse().withStatus(418).withBody("{\"error\":42}"));

        DidwwAuthException e = authError(() -> provider().token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.UNEXPECTED_STATUS);
        assertThat(e.getHttpStatus()).isEqualTo(418);
        assertThat(e.getError()).isNull();
        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testRetryStopsInsideRateLimitWindow() {
        stubToken(aResponse().withStatus(503));
        AtomicReference<TokenProvider> ref = new AtomicReference<>();
        ref.set(provider());
        ref.get().setSleeper(d -> ref.get().blockFor(Duration.ofSeconds(30)));

        DidwwAuthException e = authError(() -> ref.get().token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.RATE_LIMITED);
        assertThat(e.getRetryAfter()).isEqualTo(Duration.ofSeconds(30));
        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testMintDoesNotFollowRedirects() {
        stubToken(aResponse().withStatus(307).withHeader("Location", origin() + "/elsewhere"));
        wireMock.stubFor(post(urlEqualTo("/elsewhere")).willReturn(okJson(tokenJson("t", API))));

        DidwwAuthException e = authError(() -> provider().token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.UNEXPECTED_STATUS);
        assertThat(e.getHttpStatus()).isEqualTo(307);
        assertThat(e.getMessage()).contains("redirects are not followed");
        wireMock.verify(0, postRequestedFor(urlEqualTo("/elsewhere")));
    }

    // --- transport failures ---

    @Test
    void testMintTimeoutRetried() {
        stubTokenSequence(okJson(tokenJson("slow", API)).withFixedDelay(2000), okJson(tokenJson("t", API)));
        TokenProvider provider = provider();
        provider.setMintTimeout(Duration.ofMillis(200));

        AccessToken token = provider.token(API);

        assertThat(token.getAccessToken()).isEqualTo("t");
        assertThat(sleeps).hasSize(1);
    }

    @Test
    void testMintTimeoutExhaustsAttempts() {
        stubToken(okJson(tokenJson("slow", API)).withFixedDelay(2000));
        TokenProvider provider = provider();
        provider.setMintTimeout(Duration.ofMillis(200));

        DidwwAuthException e = authError(() -> provider.token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.CONNECTION_ERROR);
        assertThat(e.getHttpStatus()).isNull();
        assertThat(e.getCause()).isInstanceOf(InterruptedIOException.class);
        assertThat(sleeps).hasSize(TokenProvider.MAX_MINT_ATTEMPTS - 1);
    }

    @Test
    void testConnectionErrorRetriedAndKeepsNoSecret() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            port = socket.getLocalPort();
        }

        DidwwAuthException e = authError(() -> provider("http://127.0.0.1:" + port, API_KEY, null, "2026-10-22",
                new MemoryTokenStore()).token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.CONNECTION_ERROR);
        assertThat(e.getCause()).isInstanceOf(IOException.class);
        assertThat(sleeps).hasSize(TokenProvider.MAX_MINT_ATTEMPTS - 1);
        assertThat(chain(e)).doesNotContain(API_KEY);
    }

    @Test
    void testTlsAlertNotRetried() throws IOException {
        try (RawHttpServer server = RawHttpServer.tlsAlert()) {
            DidwwAuthException e = authError(() -> provider(server.httpsUrl(), API_KEY, null, "2026-10-22",
                    new MemoryTokenStore()).token(API));

            assertThat(e.getKind()).isEqualTo(AuthErrorKind.CONNECTION_ERROR);
            assertThat(e.getCause()).isInstanceOf(SSLException.class);
            assertThat(sleeps).isEmpty();
            assertThat(server.accepts()).isEqualTo(1);
        }
    }

    @Test
    void testUntrustedCertificateNotRetried() {
        WireMockServer https = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort().dynamicHttpsPort().bindAddress("127.0.0.1"));
        https.start();
        try {
            https.stubFor(post(urlEqualTo("/token")).willReturn(okJson(tokenJson("t", API))));

            DidwwAuthException e = authError(() -> provider("https://127.0.0.1:" + https.httpsPort(), API_KEY, null,
                    "2026-10-22", new MemoryTokenStore()).token(API));

            assertThat(e.getKind()).isEqualTo(AuthErrorKind.CONNECTION_ERROR);
            assertThat(TokenProvider.isTlsError(e.getCause())).isTrue();
            assertThat(sleeps).isEmpty();
            https.verify(0, postRequestedFor(urlEqualTo("/token")));
        } finally {
            https.stop();
        }
    }

    @Test
    void testConnectionClosedDuringHandshakeRetried() throws IOException {
        try (RawHttpServer server = RawHttpServer.closingDuringHandshake()) {
            DidwwAuthException e = authError(() -> provider(server.httpsUrl(), API_KEY, null, "2026-10-22",
                    new MemoryTokenStore()).token(API));

            assertThat(e.getKind()).isEqualTo(AuthErrorKind.CONNECTION_ERROR);
            assertThat(TokenProvider.isTlsError(e.getCause())).isFalse();
            assertThat(sleeps).hasSize(TokenProvider.MAX_MINT_ATTEMPTS - 1);
            assertThat(server.accepts()).isEqualTo(TokenProvider.MAX_MINT_ATTEMPTS);
        }
    }

    @Test
    void testIsTlsError() {
        assertThat(TokenProvider.isTlsError(new SSLHandshakeException("Received fatal alert: handshake_failure"))).isTrue();
        assertThat(TokenProvider.isTlsError(new SSLException("Unsupported or unrecognized SSL message"))).isTrue();
        assertThat(TokenProvider.isTlsError(new IOException(new SSLPeerUnverifiedException("Hostname not verified")))).isTrue();
        SSLHandshakeException eof = new SSLHandshakeException("Remote host terminated the handshake");
        eof.initCause(new EOFException("SSL peer shut down incorrectly"));
        assertThat(TokenProvider.isTlsError(eof)).isFalse();
        SSLException reset = new SSLException("reset", new SocketException("Connection reset"));
        assertThat(TokenProvider.isTlsError(reset)).isFalse();
        assertThat(TokenProvider.isTlsError(new SocketTimeoutException("Read timed out"))).isFalse();
        assertThat(TokenProvider.isTlsError(new IOException("unexpected end of stream"))).isFalse();
    }

    @Test
    void testCutTokenAnswerKeepsNoToken() throws IOException {
        byte[] answer = ("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 1000\r\n\r\n"
                + "{\"access_token\":\"leaked-token-value\",\"token_type\":\"Bearer\"").getBytes(StandardCharsets.ISO_8859_1);
        try (RawHttpServer server = RawHttpServer.respondingWith(answer)) {
            DidwwAuthException e = authError(() -> provider(server.url(), API_KEY, null, "2026-10-22",
                    new MemoryTokenStore()).token(API));

            assertThat(e.getKind()).isEqualTo(AuthErrorKind.CONNECTION_ERROR);
            assertThat(chain(e)).doesNotContain("leaked-token-value").doesNotContain(API_KEY);
            assertThat(server.accepts()).isEqualTo(TokenProvider.MAX_MINT_ATTEMPTS);
        }
    }

    @Test
    void testOtherErrorDuringMintKeepsNoApiKey() {
        String apiKey = "s\u00e9cretKEY";

        DidwwAuthException e = authError(() -> provider(origin(), apiKey, null, "2026-10-22",
                new MemoryTokenStore()).token(API));

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.CONNECTION_ERROR);
        assertThat(e.getCause()).isNull();
        assertThat(e.getMessage()).contains("IllegalArgumentException").doesNotContain("cretKEY");
        assertThat(mints()).isZero();
    }

    // --- grant checks and logging ---

    @Test
    void testInvalidGrant() {
        String[] bodies = {
                tokenJson("t", SMS),
                tokenJson("", API),
                "{\"token_type\":\"Bearer\",\"aud\":[\"api.didww.com\"]}",
                "not json at all",
                "[\"access_token\"]",
                tokenJson("abc\\ndef", API),
                tokenJson("abc def", API),
                "{\"access_token\":42,\"aud\":[\"api.didww.com\"]}",
        };
        for (String body : bodies) {
            wireMock.resetAll();
            stubToken(okJson(body));
            TestStores.Recording store = new TestStores.Recording();

            DidwwAuthException e = authError(() -> provider(store).token(API));

            assertThat(e.getKind()).as(body).isEqualTo(AuthErrorKind.INVALID_GRANT);
            assertThat(e.getHttpStatus()).isEqualTo(200);
            assertThat(e.getCause()).isNull();
            assertThat(e.getMessage()).doesNotContain("abc");
            assertThat(store.keys).isEmpty();
        }
    }

    @Test
    void testInvalidGrantNamesTheAudiences() {
        stubToken(okJson(tokenJson("t", SMS)));

        DidwwAuthException e = authError(() -> provider().token(API));

        assertThat(e.getErrorDescription()).isEqualTo("granted audience [api-sms.didww.com], requested api.didww.com");
    }

    @Test
    void testAudienceAsSingleString() {
        stubToken(okJson("{\"access_token\":\"t\",\"expires_in\":900,\"aud\":\"api.didww.com\",\"scope\":\"full\","
                + "\"api_version\":\"2026-10-22\"}"));

        assertThat(provider().token(API).getAudience()).containsExactly(API);
    }

    @Test
    void testScopeMismatchWarning() {
        stubToken(okJson(tokenJson("t", API)));

        provider(origin(), API_KEY, DidwwScope.READ, "2026-10-22", new MemoryTokenStore()).token(API);

        assertThat(logger.messages(System.Logger.Level.WARNING))
                .containsExactly("DIDWW token scope mismatch: requested read, granted full");
    }

    @Test
    void testApiVersionMismatchWarning() {
        stubToken(okJson(tokenJson("t", API)));

        provider(origin(), API_KEY, null, "2099-01-01", new MemoryTokenStore()).token(API);

        assertThat(logger.messages(System.Logger.Level.WARNING))
                .containsExactly("DIDWW token api_version mismatch: requested 2099-01-01, granted 2026-10-22");
    }

    @Test
    void testNoWarningWhenGrantMatches() {
        stubToken(okJson(tokenJson("t", API)));

        provider(origin(), API_KEY, DidwwScope.FULL, "2026-10-22", new MemoryTokenStore()).token(API);

        assertThat(logger.entries()).isEmpty();
    }

    @Test
    void testNoScopeWarningWithoutRequestedScope() {
        stubToken(okJson(tokenJson("t", API).replace("\"full\"", "\"read\"")));

        provider().token(API);

        assertThat(logger.entries()).isEmpty();
    }

    @Test
    void testMintedDebugLog() {
        stubToken(okJson(tokenJson("secret-token-value", API)));
        RecordingLogger debug = new RecordingLogger(true);
        TokenProvider provider = new TokenProvider(HTTP, origin(), API_KEY, "2026-10-22", null,
                new MemoryTokenStore(), debug);

        provider.token(API);

        List<String> messages = debug.messages(System.Logger.Level.DEBUG);
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).startsWith("DIDWW token minted: aud [api.didww.com], scope full, "
                + "api_version 2026-10-22, expires_in 900, duration_ms ");
        for (RecordingLogger.Entry entry : debug.entries()) {
            assertThat(entry.message).doesNotContain("secret-token-value").doesNotContain(API_KEY);
        }
    }

    @Test
    void testWithoutLoggerNothingIsLogged() {
        stubToken(okJson(tokenJson("t", API)));
        TokenProvider provider = new TokenProvider(HTTP, origin(), API_KEY, "2099-01-01", DidwwScope.READ,
                new TestStores.Failing(true, true, true), null);

        assertThat(provider.token(API).getAccessToken()).isEqualTo("t");
    }

    @Test
    void testFailingLoggerDoesNotFailTheCall() {
        stubToken(okJson(tokenJson("t", API)));
        System.Logger failing = new RecordingLogger(true) {
            @Override
            public boolean isLoggable(Level level) {
                throw new IllegalStateException("logger failed");
            }

            @Override
            public void log(Level level, java.util.ResourceBundle bundle, String msg, Throwable thrown) {
                throw new IllegalStateException("logger failed");
            }

            @Override
            public void log(Level level, java.util.ResourceBundle bundle, String format, Object... params) {
                throw new IllegalStateException("logger failed");
            }
        };
        TokenProvider provider = new TokenProvider(HTTP, origin(), API_KEY, "2099-01-01", DidwwScope.READ,
                new TestStores.Failing(true, true, false), failing);

        assertThat(provider.token(API).getAccessToken()).isEqualTo("t");
    }

    @Test
    void testAuthExceptionMessage() {
        assertThat(new DidwwAuthException(AuthErrorKind.INVALID_CLIENT, 401, "invalid_client", "bad key", null, null))
                .hasMessage("Token service: invalid_client: bad key");
        assertThat(new DidwwAuthException(AuthErrorKind.INVALID_CLIENT, 401, "invalid_client", null, null, null))
                .hasMessage("Token service: invalid_client");
        assertThat(new DidwwAuthException(AuthErrorKind.INVALID_GRANT, 200, null, "granted audience []", null, null))
                .hasMessage("Token service: granted audience []");
        assertThat(new DidwwAuthException(AuthErrorKind.CONNECTION_ERROR, null, null, null, null,
                new IOException("connection refused")))
                .hasMessage("Token service: connection refused");
        assertThat(new DidwwAuthException(AuthErrorKind.SERVER_ERROR, 503, null, null, null, null))
                .hasMessage("Token service responded with status 503");
    }
}
