package com.didww.sdk;

import com.didww.sdk.auth.MemoryTokenStore;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public abstract class BaseTest {
    protected static final String TEST_API_KEY = "test-api-key";
    protected static final String TEST_ACCESS_TOKEN = "test-access-token";
    protected static final String API_AUDIENCE = "api.didww.com";

    protected WireMockServer wireMock;
    protected DidwwClient client;

    protected String loadFixture(String path) {
        return new String(loadFixtureBytes(path));
    }

    protected byte[] loadFixtureBytes(String path) {
        try {
            return getClass().getClassLoader()
                    .getResourceAsStream("fixtures/" + path).readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fixture: " + path, e);
        }
    }

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().bindAddress("127.0.0.1"));
        wireMock.start();
        stubToken(API_AUDIENCE, TEST_ACCESS_TOKEN);
        client = clientBuilder().build();
    }

    @AfterEach
    void tearDown() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    /** The WireMock origin, {@code http://127.0.0.1:<port>}. */
    protected String origin() {
        return "http://127.0.0.1:" + wireMock.port();
    }

    /**
     * A builder for a client of the WireMock server: every service URL points to
     * it and tokens are kept in a store of this client only.
     */
    protected DidwwClient.Builder clientBuilder() {
        return DidwwClient.builder()
                .credentials(new DidwwCredentials(TEST_API_KEY, DidwwEnvironment.SANDBOX))
                .baseUrl(origin() + "/v3")
                .authUrl(origin())
                .voiceInPricesUrl(origin())
                .voiceOutPricesUrl(origin())
                .smsPricesUrl(origin())
                .tokenStore(new MemoryTokenStore())
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5));
    }

    /** Answers token requests for {@code audience} with {@code accessToken}. */
    protected void stubToken(String audience, String accessToken) {
        wireMock.stubFor(post(urlEqualTo("/token"))
                .withRequestBody(matchingJsonPath("$.aud[0]", equalTo(audience)))
                .willReturn(okJson(tokenJson(accessToken, audience))));
    }

    protected static String tokenJson(String accessToken, String audience) {
        return "{\"access_token\":\"" + accessToken + "\",\"token_type\":\"Bearer\",\"expires_in\":900,"
                + "\"aud\":[\"" + audience + "\"],\"scope\":\"full\",\"api_version\":\"2026-10-22\"}";
    }

    /** The number of token requests WireMock received. */
    protected int mints() {
        return wireMock.findAll(postRequestedFor(urlEqualTo("/token"))).size();
    }

    protected void stubGetFixture(String urlPath, String fixture) {
        wireMock.stubFor(get(urlPathEqualTo(urlPath))
                .withHeader("Authorization", equalTo("Bearer " + TEST_ACCESS_TOKEN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture(fixture))));
    }
}
