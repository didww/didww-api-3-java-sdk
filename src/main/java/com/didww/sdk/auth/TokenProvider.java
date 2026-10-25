package com.didww.sdk.auth;

import com.didww.sdk.SdkVersion;
import com.didww.sdk.exception.AuthErrorKind;
import com.didww.sdk.exception.DidwwAuthException;
import com.didww.sdk.internal.Bodies;
import com.didww.sdk.internal.Hex;
import com.didww.sdk.internal.Jitter;
import com.didww.sdk.internal.Sleeper;
import com.didww.sdk.internal.Urls;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * Internal, not part of the SDK API: mints access tokens per audience with
 * {@code POST {authUrl}/token}, caches them in the {@link TokenStore} and lets
 * concurrent callers of one client share one mint.
 *
 * <p>5xx answers and transport failures other than TLS errors are tried up
 * to {@value #MAX_MINT_ATTEMPTS} times in all, with a full-jitter backoff between
 * attempts. A 429 answer
 * blocks further mints of the same token service and API key, in this process,
 * until its Retry-After time plus a small random delay has passed.
 */
public final class TokenProvider {
    static final String CACHE_KEY_PREFIX = "didww:token:v1:";
    static final int MAX_MINT_ATTEMPTS = 4;
    static final Duration DEFAULT_MINT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_STORE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration BACKOFF_BASE = Duration.ofMillis(500);
    private static final Duration BACKOFF_CAP = Duration.ofSeconds(8);
    private static final MediaType JSON = MediaType.parse("application/json");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String STORE_READ_FAILED = "DIDWW token store read failed";
    private static final String STORE_WRITE_FAILED = "DIDWW token store write failed";
    private static final String STORE_WRITE_SKIPPED = "DIDWW token store write skipped";
    private static final String STORE_DELETE_FAILED = "DIDWW token store delete failed";
    private static final Object STORE_FAILED = new Object();

    // The 429 windows of the token service per auth URL and API key, shared by
    // every client of the process. Values are nanoTime deadlines.
    private static final ConcurrentMap<String, Long> RATE_LIMITS = new ConcurrentHashMap<>();

    private final OkHttpClient httpClient;
    private final String authUrl;
    private final HttpUrl tokenUrl;
    private final String apiKey;
    private final String apiKeyDigest;
    private final String apiVersion;
    private final String scope;
    private final TokenStore store;
    private final System.Logger logger;

    private final ConcurrentMap<String, CompletableFuture<AccessToken>> inflight = new ConcurrentHashMap<>();
    // Per cache key, the store lock keeps the write of a minted token and the
    // compare-and-delete of invalidate atomic against each other within this
    // provider; clients or processes sharing a store are not serialized.
    private final ConcurrentMap<String, ReentrantLock> storeLocks = new ConcurrentHashMap<>();
    // Per cache key, the refused access token that a store failing to evict it
    // may still return, until a new token is stored.
    private final ConcurrentMap<String, String> refused = new ConcurrentHashMap<>();

    private Sleeper sleeper = Sleeper.SYSTEM;
    private DoubleSupplier random = Jitter.SECURE_RANDOM;
    private LongSupplier nanoTime = System::nanoTime;
    private Clock clock = Clock.systemUTC();
    private Duration mintTimeout = DEFAULT_MINT_TIMEOUT;
    private Duration storeTimeout = DEFAULT_STORE_TIMEOUT;

    /**
     * Creates a provider.
     *
     * @param httpClient the client for token requests; it must not authenticate
     *                   with a bearer token and should not follow redirects
     * @param authUrl    the token service base URL
     * @param apiKey     the API key
     * @param apiVersion the API version requested for tokens
     * @param scope      the scope requested for tokens, or {@code null}
     * @param store      the token cache
     * @param logger     the logger for warnings and debug events, or {@code null} for none
     */
    public TokenProvider(OkHttpClient httpClient, String authUrl, String apiKey, String apiVersion,
                         DidwwScope scope, TokenStore store, System.Logger logger) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.authUrl = Urls.trimTrailingSlash(Objects.requireNonNull(authUrl, "authUrl must not be null"));
        this.tokenUrl = HttpUrl.parse(this.authUrl + "/token");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
        this.apiKeyDigest = Hex.sha256(apiKey);
        this.apiVersion = Objects.requireNonNull(apiVersion, "apiVersion must not be null");
        this.scope = scope != null ? scope.getValue() : null;
        this.store = Objects.requireNonNull(store, "store must not be null");
        this.logger = logger != null ? logger : SilentLogger.INSTANCE;
    }

    void setSleeper(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    void setRandom(DoubleSupplier random) {
        this.random = random;
    }

    void setNanoTime(LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    void setMintTimeout(Duration mintTimeout) {
        this.mintTimeout = mintTimeout;
    }

    void setStoreTimeout(Duration storeTimeout) {
        this.storeTimeout = storeTimeout;
    }

    /**
     * Returns the cached token for {@code audience} or mints one. Concurrent
     * callers for the same audience wait for a single mint, which runs on the
     * thread of the first caller; interrupting a waiter does not cancel it.
     *
     * @param audience the service host name, for example {@code api.didww.com}
     * @return the token
     * @throws DidwwAuthException if no token can be obtained
     */
    public AccessToken token(String audience) {
        String key = cacheKey(audience);
        AccessToken cached = readCached(key);
        if (cached != null) {
            return cached;
        }

        CompletableFuture<AccessToken> mine = new CompletableFuture<>();
        CompletableFuture<AccessToken> flight = inflight.putIfAbsent(key, mine);
        if (flight != null) {
            return await(flight);
        }
        try {
            AccessToken token = readCached(key);
            if (token == null) {
                token = mint(audience);
                storeToken(key, token);
            }
            mine.complete(token);
            return token;
        } catch (RuntimeException | Error e) {
            mine.completeExceptionally(e);
            throw e;
        } finally {
            inflight.remove(key, mine);
        }
    }

    /**
     * Evicts the cached token for {@code audience} only while it is still the
     * refused one, so that a token another caller has just minted survives. The
     * refused token is remembered, so that {@link #token(String)} mints a new
     * one even when the store keeps it.
     *
     * @param audience           the service host name
     * @param refusedAccessToken the access token the service refused
     */
    public void invalidate(String audience, String refusedAccessToken) {
        String key = cacheKey(audience);
        refused.put(key, refusedAccessToken);
        ReentrantLock lock = storeLock(key);
        try {
            if (!lock.tryLock(storeTimeout.toNanos(), TimeUnit.NANOSECONDS)) {
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        try {
            Object stored = callStore(() -> store.get(key), STORE_READ_FAILED);
            if (!(stored instanceof AccessToken) || !sameToken(((AccessToken) stored).getAccessToken(), refusedAccessToken)) {
                return;
            }
            callStore(() -> {
                store.delete(key);
                return null;
            }, STORE_DELETE_FAILED);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the cache key of the tokens for {@code audience}.
     *
     * @param audience the service host name
     * @return the key, {@code didww:token:v1:} and a SHA-256 hex digest
     */
    public String cacheKey(String audience) {
        String material = String.join("\n", authUrl, apiKeyDigest, audience, apiVersion, scope != null ? scope : "-");
        return CACHE_KEY_PREFIX + Hex.sha256(material);
    }

    /** Forgets every 429 window of the process (tests). */
    static void forgetRateLimits() {
        RATE_LIMITS.clear();
    }

    /** Opens a 429 window of this token service and API key ending after {@code duration} (tests). */
    void blockFor(Duration duration) {
        RATE_LIMITS.put(rateLimitKey(), nanoTime.getAsLong() + duration.toNanos());
    }

    Duration backoff(int attempt) {
        return Jitter.backoff(attempt, BACKOFF_BASE, BACKOFF_CAP, random);
    }

    private AccessToken readCached(String key) {
        Object stored = callStore(() -> store.get(key), STORE_READ_FAILED);
        if (!(stored instanceof AccessToken)) {
            return null;
        }
        AccessToken token = (AccessToken) stored;
        if (token.getAccessToken().isEmpty()) {
            return null;
        }
        String refusedToken = refused.get(key);
        if (refusedToken != null && sameToken(refusedToken, token.getAccessToken())) {
            return null;
        }
        return token;
    }

    private void storeToken(String key, AccessToken token) {
        String refusedBefore = refused.get(key);
        ReentrantLock lock = storeLock(key);
        try {
            if (!lock.tryLock(storeTimeout.toNanos(), TimeUnit.NANOSECONDS)) {
                warn(STORE_WRITE_SKIPPED, new TimeoutException("the store lock was not free within " + storeTimeout.toMillis() + " ms"));
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            warn(STORE_WRITE_SKIPPED, e);
            return;
        }
        try {
            Object result = callStore(() -> {
                store.set(key, token, Duration.ofSeconds(token.getExpiresIn()));
                return null;
            }, STORE_WRITE_FAILED);
            if (result != STORE_FAILED && refusedBefore != null) {
                refused.remove(key, refusedBefore);
            }
        } finally {
            lock.unlock();
        }
    }

    ReentrantLock storeLock(String key) {
        return storeLocks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    // Runs a store call, bounded by storeTimeout unless the store is the
    // in-memory one. Returns STORE_FAILED after logging a failure.
    private Object callStore(Callable<Object> call, String failureMessage) {
        if (store instanceof MemoryTokenStore) {
            try {
                return call.call();
            } catch (Exception e) {
                warn(failureMessage, e);
                return STORE_FAILED;
            }
        }

        Future<Object> future;
        try {
            future = StoreExecutor.INSTANCE.submit(call);
        } catch (RejectedExecutionException e) {
            warn(failureMessage, e);
            return STORE_FAILED;
        }
        try {
            return future.get(storeTimeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            warn(failureMessage, new TimeoutException("the store call did not finish within " + storeTimeout.toMillis() + " ms"));
            return STORE_FAILED;
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            warn(failureMessage, e);
            return STORE_FAILED;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            warn(failureMessage, cause);
            return STORE_FAILED;
        }
    }

    private AccessToken await(CompletableFuture<AccessToken> flight) {
        try {
            return flight.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DidwwAuthException(AuthErrorKind.CONNECTION_ERROR, null, null,
                    "interrupted while waiting for an access token", null, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new DidwwAuthException(AuthErrorKind.CONNECTION_ERROR, null, null, null, null, cause);
        }
    }

    private AccessToken mint(String audience) {
        throwIfRateLimited();

        ObjectNode request = MAPPER.createObjectNode();
        request.putArray("aud").add(audience);
        request.put("api_version", apiVersion);
        if (scope != null) {
            request.put("scope", scope);
        }
        byte[] body;
        try {
            body = MAPPER.writeValueAsBytes(request);
        } catch (IOException e) {
            throw new DidwwAuthException(AuthErrorKind.CONNECTION_ERROR, null, null,
                    "the token request could not be built", null, null);
        }

        long start = nanoTime.getAsLong();
        TokenResponse response = post(body);
        AccessToken token = tokenFrom(response, audience);
        if (isLoggable(System.Logger.Level.DEBUG)) {
            log(System.Logger.Level.DEBUG, "DIDWW token minted: aud " + token.getAudience()
                    + ", scope " + token.getScope()
                    + ", api_version " + token.getApiVersion()
                    + ", expires_in " + token.getExpiresIn()
                    + ", duration_ms " + TimeUnit.NANOSECONDS.toMillis(nanoTime.getAsLong() - start), null);
        }
        return token;
    }

    // Sends the token request, retrying 5xx answers and transport failures
    // other than TLS errors with a full-jitter backoff.
    private TokenResponse post(byte[] body) {
        if (tokenUrl == null) {
            throw new DidwwAuthException(AuthErrorKind.CONNECTION_ERROR, null, null,
                    "invalid token service URL", null, null);
        }
        for (int attempt = 1; ; attempt++) {
            TokenResponse response = null;
            IOException failure = null;
            try {
                response = attemptOrFail(body);
            } catch (IOException e) {
                failure = e;
            }

            if (response != null && (response.status < 500 || attempt == MAX_MINT_ATTEMPTS)) {
                return response;
            }
            if (failure != null && (Thread.currentThread().isInterrupted() || isTlsError(failure)
                    || attempt == MAX_MINT_ATTEMPTS)) {
                throw new DidwwAuthException(AuthErrorKind.CONNECTION_ERROR, null, null, null, null, failure);
            }

            sleepBeforeRetry(attempt);
            throwIfRateLimited();
        }
    }

    private TokenResponse attemptOrFail(byte[] body) throws IOException {
        try {
            return attempt(body);
        } catch (RuntimeException e) {
            // Its message may quote a header value, so only its class is kept.
            throw new DidwwAuthException(AuthErrorKind.CONNECTION_ERROR, null, null,
                    "the token request failed: " + e.getClass().getName(), null, null);
        }
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            sleeper.sleep(backoff(attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DidwwAuthException(AuthErrorKind.CONNECTION_ERROR, null, null,
                    "interrupted while waiting to retry the token request", null, e);
        }
    }

    private TokenResponse attempt(byte[] body) throws IOException {
        Request request = new Request.Builder()
                .url(tokenUrl)
                .post(RequestBody.create(body, JSON))
                .header("Accept", "application/json")
                .header("Api-Key", apiKey)
                .header("User-Agent", SdkVersion.userAgent())
                .tag(Audience.class, Audience.NONE)
                .build();
        Call call = httpClient.newCall(request);
        call.timeout().timeout(mintTimeout.toNanos(), TimeUnit.NANOSECONDS);
        try (Response response = call.execute()) {
            byte[] bytes = response.code() == 200
                    ? Bodies.readLimited(response.body(), Bodies.MAX_ERROR_BODY_SIZE, 0)
                    : Bodies.readLimitedQuietly(response.body(), Bodies.MAX_ERROR_BODY_SIZE, 0);
            return new TokenResponse(response.code(), response.header("Retry-After"), bytes);
        }
    }

    private AccessToken tokenFrom(TokenResponse response, String audience) {
        JsonNode fields;
        try {
            fields = MAPPER.readTree(response.body);
        } catch (IOException | RuntimeException e) {
            fields = null;
        }
        if (fields != null && !fields.isObject()) {
            fields = null;
        }
        String error = textField(fields, "error");
        String description = textField(fields, "error_description");

        int status = response.status;
        if (status == 200) {
            AccessToken token = new AccessToken(
                    textField(fields, "access_token"),
                    textField(fields, "token_type"),
                    intField(fields, "expires_in"),
                    audienceField(fields),
                    textField(fields, "scope"),
                    textField(fields, "api_version"),
                    clock.instant());
            verifyGrant(token, audience);
            return token;
        }
        if (status == 429) {
            Duration retryAfter = retryAfter(response.retryAfter);
            RATE_LIMITS.put(rateLimitKey(), nanoTime.getAsLong() + Jitter.spread(retryAfter, random).toNanos());
            throw new DidwwAuthException(AuthErrorKind.RATE_LIMITED, status, error, description, retryAfter, null);
        }
        AuthErrorKind kind;
        if (status == 400) {
            kind = AuthErrorKind.INVALID_REQUEST;
        } else if (status == 401) {
            kind = AuthErrorKind.INVALID_CLIENT;
        } else if (status >= 500 && status <= 599) {
            kind = AuthErrorKind.SERVER_ERROR;
        } else {
            kind = AuthErrorKind.UNEXPECTED_STATUS;
            if (status >= 300 && status <= 399 && error == null && description == null) {
                description = "the token request was redirected; redirects are not followed";
            }
        }
        throw new DidwwAuthException(kind, status, error, description, null, null);
    }

    private void verifyGrant(AccessToken token, String audience) {
        if (!isHeaderToken(token.getAccessToken())) {
            throw new DidwwAuthException(AuthErrorKind.INVALID_GRANT, 200, null,
                    "the granted access token is empty or not a valid header value", null, null);
        }
        if (!token.getAudience().contains(audience)) {
            throw new DidwwAuthException(AuthErrorKind.INVALID_GRANT, 200, null,
                    "granted audience " + token.getAudience() + ", requested " + audience, null, null);
        }
        if (scope != null && !scope.equals(token.getScope())) {
            log(System.Logger.Level.WARNING, "DIDWW token scope mismatch: requested " + scope
                    + ", granted " + token.getScope(), null);
        }
        if (!apiVersion.equals(token.getApiVersion())) {
            log(System.Logger.Level.WARNING, "DIDWW token api_version mismatch: requested " + apiVersion
                    + ", granted " + token.getApiVersion(), null);
        }
    }

    // Throws RATE_LIMITED while a 429 window of the token service is open, so
    // that no request is sent inside it.
    private void throwIfRateLimited() {
        String key = rateLimitKey();
        Long until = RATE_LIMITS.get(key);
        if (until == null) {
            return;
        }
        long remaining = until - nanoTime.getAsLong();
        if (remaining <= 0) {
            RATE_LIMITS.remove(key, until);
            return;
        }
        long seconds = (remaining + 999_999_999L) / 1_000_000_000L;
        throw new DidwwAuthException(AuthErrorKind.RATE_LIMITED, 429, "rate_limit_exceeded", null,
                Duration.ofSeconds(seconds), null);
    }

    private String rateLimitKey() {
        return authUrl + "\n" + apiKeyDigest;
    }

    // The Retry-After seconds of a 429, at least one; an HTTP date or a missing
    // value counts as one second.
    static Duration retryAfter(String header) {
        if (header == null) {
            return Duration.ofSeconds(1);
        }
        String value = header.trim();
        if (!value.matches("\\d{1,9}")) {
            return Duration.ofSeconds(1);
        }
        return Duration.ofSeconds(Math.max(1, Long.parseLong(value)));
    }

    /**
     * Returns whether {@code failure} is a certificate or TLS protocol failure,
     * which retrying cannot fix. A TLS exception caused by the connection being
     * closed or reset during the handshake is a dropped connection and is retried.
     */
    static boolean isTlsError(Throwable failure) {
        for (Throwable e = failure; e != null; e = e.getCause()) {
            if (e instanceof SSLException && !causedByDroppedConnection(e)) {
                return true;
            }
        }
        return false;
    }

    private static boolean causedByDroppedConnection(Throwable e) {
        for (Throwable c = e.getCause(); c != null; c = c.getCause()) {
            if (c instanceof EOFException || c instanceof SocketException || c instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHeaderToken(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 0x21 || c > 0x7e) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameToken(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static String textField(JsonNode fields, String name) {
        JsonNode node = fields != null ? fields.get(name) : null;
        return node != null && node.isTextual() ? node.textValue() : null;
    }

    private static int intField(JsonNode fields, String name) {
        JsonNode node = fields != null ? fields.get(name) : null;
        if (node != null && node.isIntegralNumber() && node.canConvertToInt()) {
            return Math.max(0, node.intValue());
        }
        return 0;
    }

    private static List<String> audienceField(JsonNode fields) {
        JsonNode node = fields != null ? fields.get("aud") : null;
        if (node == null) {
            return Collections.emptyList();
        }
        if (node.isTextual()) {
            return Collections.singletonList(node.textValue());
        }
        List<String> audience = new ArrayList<>();
        if (node instanceof ArrayNode) {
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    audience.add(item.textValue());
                }
            }
        }
        return audience;
    }

    private void warn(String message, Throwable failure) {
        log(System.Logger.Level.WARNING, message, failure);
    }

    private boolean isLoggable(System.Logger.Level level) {
        try {
            return logger.isLoggable(level);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void log(System.Logger.Level level, String message, Throwable failure) {
        try {
            if (failure != null) {
                logger.log(level, message, failure);
            } else {
                logger.log(level, message);
            }
        } catch (RuntimeException ignored) {
            // A failing logger must not fail the call.
        }
    }

    private static final class TokenResponse {
        private final int status;
        private final String retryAfter;
        private final byte[] body;

        private TokenResponse(int status, String retryAfter, byte[] body) {
            this.status = status;
            this.retryAfter = retryAfter;
            this.body = body;
        }
    }

    private static final class StoreExecutor {
        private static final AtomicInteger THREADS = new AtomicInteger();
        private static final ExecutorService INSTANCE = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "didww-token-store-" + THREADS.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    private static final class SilentLogger implements System.Logger {
        private static final SilentLogger INSTANCE = new SilentLogger();

        @Override
        public String getName() {
            return "com.didww.sdk";
        }

        @Override
        public boolean isLoggable(Level level) {
            return false;
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
            // Silent.
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String format, Object... params) {
            // Silent.
        }
    }
}
