package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class DidTest extends BaseTest {

    @Test
    void testListDids() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/dids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("dids/index.json"))));

        QueryParams params = QueryParams.builder()
                .include("order")
                .build();
        ApiResponse<List<Did>> response = client.dids().list(params);
        List<Did> dids = response.getData();

        assertThat(dids).isNotEmpty();
        assertThat(dids.get(0).getOrder()).isNotNull();
    }

    @Test
    void testFindDid() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/dids/9df99644-f1a5-4a3c-99a4-559d758eb96b"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("dids/show.json"))));

        ApiResponse<Did> response = client.dids().find("9df99644-f1a5-4a3c-99a4-559d758eb96b");
        Did did = response.getData();

        assertThat(did.getNumber()).isEqualTo("16091609123456797");
        assertThat(did.getBlocked()).isFalse();
        assertThat(did.getCapacityLimit()).isEqualTo(2);
        assertThat(did.getDescription()).isEqualTo("something");
        assertThat(did.getTerminated()).isFalse();
        assertThat(did.getAwaitingRegistration()).isFalse();
        assertThat(did.getPendingRemoval()).isFalse();
        assertThat(did.getChannelsIncludedCount()).isEqualTo(0);
        assertThat(did.getDedicatedChannelsCount()).isEqualTo(0);
    }
}
