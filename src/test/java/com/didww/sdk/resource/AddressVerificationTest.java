package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.AddressVerificationStatus;
import com.didww.sdk.resource.enums.CallbackMethod;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class AddressVerificationTest extends BaseTest {

    @Test
    void testListAddressVerifications() {
        stubGetFixture("/v3/address_verifications", "address_verifications/index.json");

        QueryParams params = QueryParams.builder()
                .include("address", "dids")
                .build();
        ApiResponse<List<AddressVerification>> response = client.addressVerifications().list(params);
        List<AddressVerification> verifications = response.getData();

        assertThat(verifications).isNotEmpty();
        assertThat(verifications.get(0).getId()).isEqualTo("aaf2180a-3f2b-4427-888f-3d00f872014e");
        assertThat(verifications.get(0).getCallbackUrl()).isEqualTo("http://example.com");
        assertThat(verifications.get(0).getCallbackMethod()).isEqualTo(CallbackMethod.GET);
        assertThat(verifications.get(0).getStatus()).isEqualTo(AddressVerificationStatus.PENDING);
        assertThat(verifications.get(0).getAddress()).isNotNull();
        assertThat(verifications.get(0).getAddress().getCityName()).isEqualTo("Chicago");
    }

    @Test
    void testFindAddressVerification() {
        stubGetFixture("/v3/address_verifications/c8e004b0-87ec-4987-b4fb-ee89db099f0e", "address_verifications/show.json");

        ApiResponse<AddressVerification> response = client.addressVerifications().find("c8e004b0-87ec-4987-b4fb-ee89db099f0e");
        AddressVerification av = response.getData();

        assertThat(av.getId()).isEqualTo("c8e004b0-87ec-4987-b4fb-ee89db099f0e");
        assertThat(av.getStatus()).isEqualTo(AddressVerificationStatus.APPROVED);
        assertThat(av.getReference()).isEqualTo("SHB-485120");
    }

    @Test
    void testFindRejectedAddressVerification() {
        stubGetFixture("/v3/address_verifications/429e6d4e-2ee9-4953-aa98-0b3ac07f0f96", "address_verifications/show_rejected.json");

        ApiResponse<AddressVerification> response = client.addressVerifications().find("429e6d4e-2ee9-4953-aa98-0b3ac07f0f96");
        AddressVerification av = response.getData();

        assertThat(av.getId()).isEqualTo("429e6d4e-2ee9-4953-aa98-0b3ac07f0f96");
        assertThat(av.getStatus()).isEqualTo(AddressVerificationStatus.REJECTED);
        assertThat(av.getRejectReasons()).isEqualTo(Arrays.asList("Address cannot be validated", "Proof of address should be not older than of 6 months"));
        assertThat(av.getReference()).isEqualTo("ODW-879912");
    }

    @Test
    void testCreateAddressVerification() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/address_verifications"))
                .withRequestBody(equalToJson(loadFixture("address_verifications/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("address_verifications/create.json"))));

        Address address = new Address().withId("d3414687-40f4-4346-a267-c2c65117d28c");
        Did did = new Did().withId("a9d64c02-4486-4acb-a9a1-be4c81ff0659");

        AddressVerification verification = new AddressVerification();
        verification.setCallbackUrl("http://example.com");
        verification.setCallbackMethod(CallbackMethod.GET);
        verification.setAddress(address);
        verification.setDids(Collections.singletonList(did));

        QueryParams createParams = QueryParams.builder()
                .include("address")
                .build();
        ApiResponse<AddressVerification> response = client.addressVerifications().create(verification, createParams);
        AddressVerification created = response.getData();

        assertThat(created.getId()).isEqualTo("78182ef2-8377-41cd-89e1-26e8266c9c94");
        assertThat(created.getCallbackUrl()).isEqualTo("http://example.com");
        assertThat(created.getCallbackMethod()).isEqualTo(CallbackMethod.GET);
        assertThat(created.getStatus()).isEqualTo(AddressVerificationStatus.PENDING);
        assertThat(created.getAddress()).isNotNull();
        assertThat(created.getAddress().getCityName()).isEqualTo("Chicago");
    }
}
