package com.didww.sdk.http;

import com.didww.sdk.BaseTest;
import com.didww.sdk.SdkVersion;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyInterceptorTest extends BaseTest {

    @Test
    void testUserAgentHeaderIsSent() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("balance/index.json"))));

        client.balance().find();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/balance"))
                .withHeader("User-Agent", equalTo(SdkVersion.userAgent())));

        assertThat(SdkVersion.get()).isNotEqualTo("unknown");
        assertThat(SdkVersion.userAgent()).isEqualTo("didww-java-sdk/" + SdkVersion.get());
    }
}
