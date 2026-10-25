package com.didww.sdk.auth;

import com.didww.sdk.SdkVersion;
import com.didww.sdk.exception.AuthErrorKind;
import com.didww.sdk.exception.DidwwAuthException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ScenarioMappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class BearerTokenInterceptorTest {
    private static final String API = "api.didww.com";
    private static final MediaType JSON_API = MediaType.parse("application/vnd.api+json");

    private WireMockServer wireMock;
    private MemoryTokenStore store;
    private TokenProvider tokens;
    private OkHttpClient http;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().bindAddress("127.0.0.1"));
        wireMock.start();
        store = new MemoryTokenStore();
        OkHttpClient base = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .readTimeout(Duration.ofSeconds(5))
                .build();
        tokens = new TokenProvider(base, origin(), "test-api-key", "2026-10-22", null, store, null);
        http = base.newBuilder().addInterceptor(new BearerTokenInterceptor(tokens)).build();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
        TokenProvider.forgetRateLimits();
    }

    private String origin() {
        return "http://127.0.0.1:" + wireMock.port();
    }

    private static String tokenJson(String accessToken, String audience) {
        return "{\"access_token\":\"" + accessToken + "\",\"token_type\":\"Bearer\",\"expires_in\":900,"
                + "\"aud\":[\"" + audience + "\"],\"scope\":\"full\",\"api_version\":\"2026-10-22\"}";
    }

    private void stubTokens(String... accessTokens) {
        for (int i = 0; i < accessTokens.length; i++) {
            ScenarioMappingBuilder mapping = post(urlEqualTo("/token"))
                    .inScenario("tokens")
                    .whenScenarioStateIs(i == 0 ? Scenario.STARTED : "s" + i)
                    .willReturn(okJson(tokenJson(accessTokens[i], API)));
            if (i + 1 < accessTokens.length) {
                mapping = mapping.willSetStateTo("s" + (i + 1));
            }
            wireMock.stubFor(mapping);
        }
    }

    private int mints() {
        return wireMock.findAll(postRequestedFor(urlEqualTo("/token"))).size();
    }

    private Response execute(Request request) throws IOException {
        return http.newCall(request).execute();
    }

    private Request.Builder request(String path) {
        return new Request.Builder().url(origin() + path);
    }

    @Test
    void testJsonApiRequestHeaders() throws IOException {
        stubTokens("token-1");
        wireMock.stubFor(get(urlEqualTo("/v3/countries")).willReturn(okJson("{\"data\":[]}")));

        try (Response response = execute(request("/v3/countries").build())) {
            assertThat(response.code()).isEqualTo(200);
        }

        wireMock.verify(getRequestedFor(urlEqualTo("/v3/countries"))
                .withHeader("Authorization", equalTo("Bearer token-1"))
                .withHeader("Accept", equalTo("application/vnd.api+json"))
                .withHeader("User-Agent", equalTo(SdkVersion.userAgent()))
                .withoutHeader("Api-Key")
                .withoutHeader("X-DIDWW-API-Version")
                .withoutHeader("Content-Type"));
        wireMock.verify(postRequestedFor(urlEqualTo("/token")).withRequestBody(matchingJsonPath("$.aud[0]", equalTo(API))));
    }

    @Test
    void testUserAgentAndAcceptOfTheCallerAreKept() throws IOException {
        stubTokens("token-1");
        wireMock.stubFor(get(urlEqualTo("/v3/countries")).willReturn(okJson("{\"data\":[]}")));

        execute(request("/v3/countries").header("User-Agent", "my-app/1.0").header("Accept", "application/json")
                .build()).close();

        wireMock.verify(getRequestedFor(urlEqualTo("/v3/countries"))
                .withHeader("User-Agent", equalTo("my-app/1.0"))
                .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void testTaggedRequestUsesItsAudienceWithoutJsonApiAccept() throws IOException {
        wireMock.stubFor(post(urlEqualTo("/token")).withRequestBody(matchingJsonPath("$.aud[0]", equalTo("api-vin.didww.com")))
                .willReturn(okJson(tokenJson("vin-token", "api-vin.didww.com"))));
        wireMock.stubFor(get(urlEqualTo("/v3/prices.csv.gz")).willReturn(ok("x")));

        execute(request("/v3/prices.csv.gz").tag(Audience.class, Audience.VOICE_IN).build()).close();

        wireMock.verify(getRequestedFor(urlEqualTo("/v3/prices.csv.gz"))
                .withHeader("Authorization", equalTo("Bearer vin-token"))
                .withoutHeader("Accept"));
    }

    @Test
    void testNoneAudienceSendsNoToken() throws IOException {
        wireMock.stubFor(get(urlEqualTo("/v3/countries")).willReturn(okJson("{\"data\":[]}")));

        execute(request("/v3/countries").header("Authorization", "Bearer user-set")
                .tag(Audience.class, Audience.NONE).build()).close();

        wireMock.verify(getRequestedFor(urlEqualTo("/v3/countries")).withoutHeader("Authorization"));
        assertThat(mints()).isZero();
    }

    @Test
    void testPublicEndpointSendsNoToken() throws IOException {
        wireMock.stubFor(get(urlEqualTo("/v3/public_keys")).willReturn(okJson("{\"data\":[]}")));

        execute(request("/v3/public_keys").build()).close();

        wireMock.verify(getRequestedFor(urlEqualTo("/v3/public_keys"))
                .withoutHeader("Authorization")
                .withHeader("Accept", equalTo("application/vnd.api+json")));
        assertThat(mints()).isZero();
    }

    @Test
    void testIsPublicEndpoint() {
        assertThat(BearerTokenInterceptor.isPublicEndpoint(HttpUrl.get("https://api.didww.com/v3/public_keys"))).isTrue();
        assertThat(BearerTokenInterceptor.isPublicEndpoint(HttpUrl.get("https://h/proxy/v3/public_keys?x=1"))).isTrue();
        assertThat(BearerTokenInterceptor.isPublicEndpoint(HttpUrl.get("https://h/v3/public_keys/1"))).isTrue();
        assertThat(BearerTokenInterceptor.isPublicEndpoint(HttpUrl.get("https://h/v3/dids/public_keys"))).isFalse();
        assertThat(BearerTokenInterceptor.isPublicEndpoint(HttpUrl.get("https://h/v3/public_keys_x"))).isFalse();
        assertThat(BearerTokenInterceptor.isPublicEndpoint(HttpUrl.get("https://h/public_keys"))).isFalse();
        assertThat(BearerTokenInterceptor.isPublicEndpoint(HttpUrl.get("https://h/v3"))).isFalse();
    }

    @Test
    void testIfNoneMatchIsStripped() throws IOException {
        stubTokens("token-1");
        wireMock.stubFor(get(urlEqualTo("/v3/countries")).willReturn(okJson("{\"data\":[]}")));

        execute(request("/v3/countries").header("If-None-Match", "\"etag\"").build()).close();

        wireMock.verify(getRequestedFor(urlEqualTo("/v3/countries")).withoutHeader("If-None-Match"));
    }

    @Test
    void testRetriesOnceWithNewTokenAfter401() throws IOException {
        stubTokens("token-1", "token-2");
        wireMock.stubFor(get(urlEqualTo("/v3/countries")).withHeader("Authorization", equalTo("Bearer token-1"))
                .willReturn(aResponse().withStatus(401)));
        wireMock.stubFor(get(urlEqualTo("/v3/countries")).withHeader("Authorization", equalTo("Bearer token-2"))
                .willReturn(okJson("{\"data\":[]}")));

        try (Response response = execute(request("/v3/countries").build())) {
            assertThat(response.code()).isEqualTo(200);
        }

        assertThat(mints()).isEqualTo(2);
        wireMock.verify(2, getRequestedFor(urlEqualTo("/v3/countries")));
        assertThat(store.get(tokens.cacheKey(API)).getAccessToken()).isEqualTo("token-2");
    }

    @Test
    void testReplaysBodyAfter401() throws IOException {
        stubTokens("token-1", "token-2");
        String body = "{\"data\":{\"type\":\"dids\",\"id\":\"1\",\"attributes\":{\"description\":\"x\"}}}";
        wireMock.stubFor(patch(urlEqualTo("/v3/dids/1")).withHeader("Authorization", equalTo("Bearer token-1"))
                .willReturn(aResponse().withStatus(401)));
        wireMock.stubFor(patch(urlEqualTo("/v3/dids/1")).withHeader("Authorization", equalTo("Bearer token-2"))
                .willReturn(okJson(body)));

        try (Response response = execute(request("/v3/dids/1").patch(RequestBody.create(body, JSON_API)).build())) {
            assertThat(response.code()).isEqualTo(200);
        }

        wireMock.verify(2, patchRequestedFor(urlEqualTo("/v3/dids/1"))
                .withHeader("Content-Type", containing("application/vnd.api+json"))
                .withRequestBody(equalToJson(body)));
    }

    @Test
    void testSecond401DropsReplacementToken() throws IOException {
        stubTokens("token-1", "token-2");
        wireMock.stubFor(get(urlEqualTo("/v3/countries")).willReturn(aResponse().withStatus(401)));

        try (Response response = execute(request("/v3/countries").build())) {
            assertThat(response.code()).isEqualTo(401);
        }

        assertThat(mints()).isEqualTo(2);
        wireMock.verify(2, getRequestedFor(urlEqualTo("/v3/countries")));
        assertThat(store.get(tokens.cacheKey(API))).isNull();
    }

    @Test
    void testForbiddenNotRetried() throws IOException {
        stubTokens("token-1", "token-2");
        wireMock.stubFor(get(urlEqualTo("/v3/countries")).willReturn(aResponse().withStatus(403)));

        try (Response response = execute(request("/v3/countries").build())) {
            assertThat(response.code()).isEqualTo(403);
        }

        assertThat(mints()).isEqualTo(1);
        wireMock.verify(1, getRequestedFor(urlEqualTo("/v3/countries")));
        assertThat(store.get(tokens.cacheKey(API)).getAccessToken()).isEqualTo("token-1");
    }

    @Test
    void testOneShotBodyNotReplayed() throws IOException {
        stubTokens("token-1", "token-2");
        wireMock.stubFor(post(urlEqualTo("/v3/encrypted_files")).willReturn(aResponse().withStatus(401)));
        RequestBody oneShot = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("data");
            }

            @Override
            public boolean isOneShot() {
                return true;
            }
        };

        try (Response response = execute(request("/v3/encrypted_files").post(oneShot).build())) {
            assertThat(response.code()).isEqualTo(401);
        }

        wireMock.verify(1, postRequestedFor(urlEqualTo("/v3/encrypted_files")));
        assertThat(mints()).isEqualTo(1);
        assertThat(store.get(tokens.cacheKey(API))).isNull();
    }

    @Test
    void testTokenErrorPropagatesUnchanged() {
        wireMock.stubFor(post(urlEqualTo("/token")).willReturn(aResponse().withStatus(401)
                .withBody("{\"error\":\"invalid_client\"}")));

        DidwwAuthException e = catchThrowableOfType(() -> execute(request("/v3/countries").build()),
                DidwwAuthException.class);

        assertThat(e).isNotNull();
        assertThat(e.getKind()).isEqualTo(AuthErrorKind.INVALID_CLIENT);
        wireMock.verify(0, getRequestedFor(urlEqualTo("/v3/countries")));
    }
}
