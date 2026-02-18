package com.didww.sdk;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;

public abstract class BaseTest {
    protected WireMockServer wireMock;
    protected DidwwClient client;

    protected String loadFixture(String path) {
        try {
            return new String(getClass().getClassLoader()
                    .getResourceAsStream("fixtures/" + path).readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fixture: " + path, e);
        }
    }

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        String baseUrl = "http://localhost:" + wireMock.port() + "/v3";
        client = DidwwClient.builder()
                .credentials(new DidwwCredentials("test-api-key", DidwwEnvironment.SANDBOX))
                .baseUrl(baseUrl)
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }
}
