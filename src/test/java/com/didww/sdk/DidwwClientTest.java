package com.didww.sdk;

import com.didww.sdk.resource.Country;
import com.didww.sdk.resource.PublicKey;
import com.didww.sdk.repository.ApiResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class DidwwClientTest {

    @Test
    void testBuilderWithProductionEnvironment() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.PRODUCTION))
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("https://api.didww.com/v3");
    }

    @Test
    void testBuilderWithSandboxEnvironment() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.SANDBOX))
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("https://sandbox-api.didww.com/v3");
    }

    @Test
    void testBuilderWithCustomBaseUrl() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.SANDBOX))
                .baseUrl("http://localhost:8080/v3")
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("http://localhost:8080/v3");
    }

    @Test
    void testBuilderRequiresCredentials() {
        assertThatThrownBy(() -> DidwwClient.builder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testBuilderWithCustomHttpClientBuilder() {
        WireMockServer wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
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

            DidwwClient client = DidwwClient.builder()
                    .credentials(new DidwwCredentials("test-key", DidwwEnvironment.SANDBOX))
                    .baseUrl("http://localhost:" + wireMock.port() + "/v3")
                    .httpClientBuilder(httpBuilder)
                    .build();

            ApiResponse<List<Country>> response = client.countries().list();
            assertThat(response.getData()).isEmpty();

            wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/countries"))
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
    }

    @Test
    void testPublicKeysDoesNotSendApiKey() {
        WireMockServer wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        try {
            wireMock.stubFor(get(urlPathEqualTo("/v3/public_keys"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/vnd.api+json")
                            .withBody("{\"data\":[]}")));

            DidwwClient client = DidwwClient.builder()
                    .credentials(new DidwwCredentials("test-key", DidwwEnvironment.SANDBOX))
                    .baseUrl("http://localhost:" + wireMock.port() + "/v3")
                    .build();

            ApiResponse<List<PublicKey>> response = client.publicKeys().list();
            assertThat(response.getData()).isEmpty();

            wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/public_keys"))
                    .withoutHeader("Api-Key"));
        } finally {
            wireMock.stop();
        }
    }

    @Test
    void testAuthenticatedEndpointSendsApiKey() {
        WireMockServer wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        try {
            wireMock.stubFor(get(urlPathEqualTo("/v3/countries"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/vnd.api+json")
                            .withBody("{\"data\":[]}")));

            DidwwClient client = DidwwClient.builder()
                    .credentials(new DidwwCredentials("test-key", DidwwEnvironment.SANDBOX))
                    .baseUrl("http://localhost:" + wireMock.port() + "/v3")
                    .build();

            ApiResponse<List<Country>> response = client.countries().list();
            assertThat(response.getData()).isEmpty();

            wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/countries"))
                    .withHeader("Api-Key", equalTo("test-key")));
        } finally {
            wireMock.stop();
        }
    }
}
