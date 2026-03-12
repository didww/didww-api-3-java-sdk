package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceTest extends BaseTest {

    @Test
    void testFindBalance() {
        stubGetFixture("/v3/balance", "balance/index.json");

        ApiResponse<Balance> response = client.balance().find();
        Balance balance = response.getData();

        assertThat(balance.getId()).isEqualTo("4c39e0bf-683b-4697-9322-5abaf4011883");
        assertThat(balance.getTotalBalance()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(balance.getCredit()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(balance.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));
    }
}
