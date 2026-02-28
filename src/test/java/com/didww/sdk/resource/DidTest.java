package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.AddressVerificationStatus;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class DidTest extends BaseTest {

    @Test
    void testUpdateDid() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/dids/9df99644-f1a5-4a3c-99a4-559d758eb96b"))
                .withRequestBody(equalToJson(loadFixture("dids/update_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("dids/update.json"))));

        Did did = Did.build("9df99644-f1a5-4a3c-99a4-559d758eb96b");
        did.setCapacityLimit(2);
        did.setDescription("something");

        ApiResponse<Did> response = client.dids().update(did);
        Did updated = response.getData();

        assertThat(updated.getId()).isEqualTo("9df99644-f1a5-4a3c-99a4-559d758eb96b");
        assertThat(updated.getNumber()).isEqualTo("16091609123456797");
        assertThat(updated.getBlocked()).isFalse();
        assertThat(updated.getCapacityLimit()).isEqualTo(2);
        assertThat(updated.getDescription()).isEqualTo("something");
        assertThat(updated.getTerminated()).isFalse();
    }

    @Test
    void testUpdateDidTerminated() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/dids/9df99644-f1a5-4a3c-99a4-559d758eb96b"))
                .withRequestBody(equalToJson(loadFixture("dids/update_terminated_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("dids/update_terminated.json"))));

        Did did = Did.build("9df99644-f1a5-4a3c-99a4-559d758eb96b");
        did.setTerminated(true);

        ApiResponse<Did> response = client.dids().update(did);
        Did updated = response.getData();

        assertThat(updated.getId()).isEqualTo("9df99644-f1a5-4a3c-99a4-559d758eb96b");
        assertThat(updated.getBlocked()).isTrue();
        assertThat(updated.getTerminated()).isTrue();
        assertThat(updated.getBillingCyclesCount()).isEqualTo(0);
    }

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
        assertThat(dids.get(0).getOrder().getReference()).isEqualTo("TZO-560180");
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
        assertThat(did.getBillingCyclesCount()).isNull();
        assertThat(did.getChannelsIncludedCount()).isEqualTo(0);
        assertThat(did.getDedicatedChannelsCount()).isEqualTo(0);
    }

    @Test
    void testFindDidWithAddressVerificationAndDidGroup() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/dids/21d0b02c-b556-4d3e-acbf-504b78295dbe"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("dids/show_with_address_verification_and_did_group.json"))));

        QueryParams params = QueryParams.builder()
                .include("address_verification", "did_group")
                .build();
        ApiResponse<Did> response = client.dids().find("21d0b02c-b556-4d3e-acbf-504b78295dbe", params);
        Did did = response.getData();

        assertThat(did.getNumber()).isEqualTo("61488943592");

        AddressVerification addressVerification = did.getAddressVerification();
        assertThat(addressVerification).isNotNull();
        assertThat(addressVerification.getId()).isEqualTo("75dc8d39-5e17-4470-a6f3-df42642c975f");
        assertThat(addressVerification.getStatus()).isEqualTo(AddressVerificationStatus.APPROVED);

        DidGroup didGroup = did.getDidGroup();
        assertThat(didGroup).isNotNull();
        assertThat(didGroup.getId()).isEqualTo("2b60bb9a-d382-4d35-84c6-61689f45f2f5");
        assertThat(didGroup.getPrefix()).isEqualTo("4");
        assertThat(didGroup.getAreaName()).isEqualTo("Mobile");
        assertThat(didGroup.getIsMetered()).isFalse();
        assertThat(didGroup.getAllowAdditionalChannels()).isFalse();
    }
}
