package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class BalanceTest extends BaseTest {

    @Test
    void testFindBalance() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("balance/index.json"))));

        ApiResponse<Balance> response = client.balance().find();
        Balance balance = response.getData();

        assertThat(balance.getId()).isEqualTo("4c39e0bf-683b-4697-9322-5abaf4011883");
        assertThat(balance.getTotalBalance()).isEqualTo(60.0);
        assertThat(balance.getCredit()).isEqualTo(10.0);
        assertThat(balance.getBalance()).isEqualTo(50.0);
    }
}
