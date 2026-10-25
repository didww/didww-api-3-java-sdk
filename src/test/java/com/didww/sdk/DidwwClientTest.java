package com.didww.sdk;

import com.didww.sdk.auth.MemoryTokenStore;
import com.didww.sdk.resource.Country;
import com.didww.sdk.resource.PublicKey;
import com.didww.sdk.repository.ApiResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import okhttp3.Authenticator;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class DidwwClientTest {

    private static WireMockServer startWireMock() {
        WireMockServer wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort().bindAddress("127.0.0.1"));
        wireMock.start();
        wireMock.stubFor(post(urlEqualTo("/token"))
                .willReturn(okJson(BaseTest.tokenJson("test-access-token", "api.didww.com"))));
        return wireMock;
    }

    private static DidwwClient.Builder clientFor(WireMockServer wireMock) {
        String origin = "http://127.0.0.1:" + wireMock.port();
        return DidwwClient.builder()
                .credentials(new DidwwCredentials("test-key", DidwwEnvironment.SANDBOX))
                .baseUrl(origin + "/v3")
                .authUrl(origin)
                .tokenStore(new MemoryTokenStore());
    }

    @Test
    void testBuilderWithProductionEnvironment() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.PRODUCTION))
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("https://api.didww.com/v3");
        assertThat(client.getAuthUrl()).isEqualTo("https://auth.didww.com");
    }

    @Test
    void testBuilderWithSandboxEnvironment() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.SANDBOX))
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("https://sandbox-api.didww.com/v3");
        assertThat(client.getAuthUrl()).isEqualTo("https://auth-sandbox.didww.com");
    }

    @Test
    void testBuilderWithCustomBaseUrl() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.SANDBOX))
                .baseUrl("http://localhost:8080/v3")
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("http://localhost:8080/v3");
        assertThat(client.getAuthUrl()).isEqualTo("https://auth-sandbox.didww.com");
    }

    @Test
    void testBuilderRequiresCredentials() {
        assertThatThrownBy(() -> DidwwClient.builder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testBuilderWithCustomHttpClientBuilder() {
        WireMockServer wireMock = startWireMock();
        try {
            wireMock.stubFor(get(urlPathEqualTo("/v3/countries"))
                    .withHeader("X-Custom-Header", equalTo("custom-value"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/vnd.api+json")
                            .withBody("{\"data\":[]}")));

            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                    .header("X-Custom-Header", "custom-value")
                                    .build()));

            DidwwClient client = clientFor(wireMock)
                    .httpClientBuilder(httpBuilder)
                    .build();

            ApiResponse<List<Country>> response = client.countries().list();
            assertThat(response.getData()).isEmpty();

            wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/countries"))
                    .withHeader("X-Custom-Header", equalTo("custom-value"))
                    .withHeader("Authorization", equalTo("Bearer test-access-token"))
                    .withoutHeader("Api-Key")
                    .withoutHeader("X-DIDWW-API-Version"));
            wireMock.verify(postRequestedFor(urlEqualTo("/token"))
                    .withHeader("X-Custom-Header", equalTo("custom-value"))
                    .withHeader("Api-Key", equalTo("test-key")));
        } finally {
            wireMock.stop();
        }
    }

    @Test
    void testBuilderWithCustomHttpClientBuilderPreservesTimeouts() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 18080));
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .proxy(proxy);

        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("test-key", DidwwEnvironment.SANDBOX))
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(25))
                .httpClientBuilder(httpBuilder)
                .build();

        OkHttpClient okHttpClient = client.getHttpClient();
        assertThat(okHttpClient.connectTimeoutMillis()).isEqualTo(15_000);
        assertThat(okHttpClient.readTimeoutMillis()).isEqualTo(25_000);
        assertThat(okHttpClient.proxy()).isEqualTo(proxy);
        assertThat(client.getTokenHttpClient().proxy()).isEqualTo(proxy);
    }

    @Test
    void testCustomHttpClientBuilderIsNotMutated() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        int interceptorsBefore = httpBuilder.interceptors().size();

        DidwwClient.builder()
                .credentials(new DidwwCredentials("test-key", DidwwEnvironment.SANDBOX))
                .connectTimeout(Duration.ofSeconds(5))
                .httpClientBuilder(httpBuilder)
                .build();

        // The caller's builder must not be modified
        assertThat(httpBuilder.interceptors()).hasSize(interceptorsBefore);
        // Building a plain client from the original builder should use default timeouts
        OkHttpClient plain = httpBuilder.build();
        assertThat(plain.connectTimeoutMillis()).isEqualTo(10_000); // OkHttp default
        assertThat(plain.followRedirects()).isTrue();
    }

    @Test
    void testPublicKeysSendNeitherTokenNorApiKey() {
        WireMockServer wireMock = startWireMock();
        try {
            wireMock.stubFor(get(urlPathEqualTo("/v3/public_keys"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/vnd.api+json")
                            .withBody("{\"data\":[]}")));

            DidwwClient client = clientFor(wireMock).build();

            ApiResponse<List<PublicKey>> response = client.publicKeys().list();
            assertThat(response.getData()).isEmpty();

            wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/public_keys"))
                    .withoutHeader("Api-Key")
                    .withoutHeader("Authorization")
                    .withoutHeader("X-DIDWW-API-Version")
                    .withHeader("Accept", equalTo("application/vnd.api+json")));
            wireMock.verify(0, postRequestedFor(urlEqualTo("/token")));
        } finally {
            wireMock.stop();
        }
    }

    @Test
    void testAuthenticatedEndpointSendsBearerToken() {
        WireMockServer wireMock = startWireMock();
        try {
            wireMock.stubFor(get(urlPathEqualTo("/v3/countries"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/vnd.api+json")
                            .withBody("{\"data\":[]}")));

            DidwwClient client = clientFor(wireMock).build();

            ApiResponse<List<Country>> response = client.countries().list();
            assertThat(response.getData()).isEmpty();

            wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/countries"))
                    .withHeader("Authorization", equalTo("Bearer test-access-token"))
                    .withHeader("Accept", equalTo("application/vnd.api+json"))
                    .withHeader("User-Agent", equalTo(SdkVersion.userAgent()))
                    .withoutHeader("Api-Key")
                    .withoutHeader("X-DIDWW-API-Version"));
            wireMock.verify(postRequestedFor(urlEqualTo("/token"))
                    .withHeader("Api-Key", equalTo("test-key"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withHeader("User-Agent", equalTo(SdkVersion.userAgent()))
                    .withoutHeader("Authorization")
                    .withRequestBody(equalToJson("{\"aud\":[\"api.didww.com\"],\"api_version\":\"2026-10-22\"}")));
        } finally {
            wireMock.stop();
        }
    }

    @Test
    void testSdkClientsDoNotFollowRedirectsOrUseCookiesCacheOrAuthenticator() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(okhttp3.HttpUrl url, List<okhttp3.Cookie> cookies) {
                    }

                    @Override
                    public List<okhttp3.Cookie> loadForRequest(okhttp3.HttpUrl url) {
                        return List.of();
                    }
                })
                .authenticator((route, response) -> null)
                .cache(new okhttp3.Cache(new java.io.File(System.getProperty("java.io.tmpdir"), "didww-sdk-test-cache"), 1024));

        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("test-key", DidwwEnvironment.SANDBOX))
                .httpClientBuilder(httpBuilder)
                .build();

        for (OkHttpClient http : List.of(client.getHttpClient(), client.getTokenHttpClient())) {
            assertThat(http.followRedirects()).isFalse();
            assertThat(http.followSslRedirects()).isFalse();
            assertThat(http.cookieJar()).isSameAs(CookieJar.NO_COOKIES);
            assertThat(http.authenticator()).isSameAs(Authenticator.NONE);
            assertThat(http.cache()).isNull();
        }
    }

    @Test
    void testDefaultTimeoutsAndSharedConnectionPool() {
        DidwwClient a = DidwwClient.builder()
                .credentials(new DidwwCredentials("key-a", DidwwEnvironment.SANDBOX))
                .build();
        DidwwClient b = DidwwClient.builder()
                .credentials(new DidwwCredentials("key-b", DidwwEnvironment.PRODUCTION))
                .build();

        OkHttpClient http = a.getHttpClient();
        assertThat(http.connectTimeoutMillis()).isEqualTo(10_000);
        assertThat(http.readTimeoutMillis()).isEqualTo(60_000);
        assertThat(http.writeTimeoutMillis()).isEqualTo(60_000);
        assertThat(http.callTimeoutMillis()).isZero();
        assertThat(http.followRedirects()).isFalse();
        assertThat(http.connectionPool()).isSameAs(b.getHttpClient().connectionPool());
        assertThat(http.connectionPool()).isSameAs(a.getTokenHttpClient().connectionPool());
        assertThat(http.dispatcher()).isSameAs(b.getHttpClient().dispatcher());
    }

    @Test
    void testDefaultTokenStoreIsSharedByTheProcess() {
        DidwwClient a = DidwwClient.builder()
                .credentials(new DidwwCredentials("key-a", DidwwEnvironment.SANDBOX))
                .build();
        DidwwClient b = DidwwClient.builder()
                .credentials(new DidwwCredentials("key-b", DidwwEnvironment.PRODUCTION))
                .build();
        MemoryTokenStore own = new MemoryTokenStore();
        DidwwClient c = DidwwClient.builder()
                .credentials(new DidwwCredentials("key-c", DidwwEnvironment.SANDBOX))
                .tokenStore(own)
                .build();

        assertThat(a.getTokenStore()).isInstanceOf(MemoryTokenStore.class).isSameAs(b.getTokenStore());
        assertThat(c.getTokenStore()).isSameAs(own);
    }

    @Test
    void testCredentialsToStringHidesApiKey() {
        DidwwCredentials credentials = new DidwwCredentials("secret-api-key-value", DidwwEnvironment.SANDBOX);

        assertThat(credentials.toString())
                .doesNotContain("secret-api-key-value")
                .contains("[FILTERED]")
                .contains("SANDBOX");
    }
}
