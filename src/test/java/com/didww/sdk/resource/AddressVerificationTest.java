package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.AddressVerificationStatus;
import com.didww.sdk.resource.enums.CallbackMethod;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class AddressVerificationTest extends BaseTest {

    @Test
    void testListAddressVerifications() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/address_verifications"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("address_verifications/index.json"))));

        ApiResponse<List<AddressVerification>> response = client.addressVerifications().list();
        List<AddressVerification> verifications = response.getData();

        assertThat(verifications).isNotEmpty();
        assertThat(verifications.get(0).getId()).isEqualTo("aaf2180a-3f2b-4427-888f-3d00f872014e");
        assertThat(verifications.get(0).getCallbackUrl()).isEqualTo("http://example.com");
        assertThat(verifications.get(0).getCallbackMethod()).isEqualTo(CallbackMethod.GET);
        assertThat(verifications.get(0).getStatus()).isEqualTo(AddressVerificationStatus.PENDING);
    }

    @Test
    void testCreateAddressVerification() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/address_verifications"))
                .withRequestBody(equalToJson(loadFixture("address_verifications/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("address_verifications/create.json"))));

        Address address = new Address();
        address.setId("d3414687-40f4-4346-a267-c2c65117d28c");

        Did did = new Did();
        did.setId("a9d64c02-4486-4acb-a9a1-be4c81ff0659");

        AddressVerification verification = new AddressVerification();
        verification.setCallbackUrl("http://example.com");
        verification.setCallbackMethod(CallbackMethod.GET);
        verification.setAddress(address);
        verification.setDids(Collections.singletonList(did));

        ApiResponse<AddressVerification> response = client.addressVerifications().create(verification);
        AddressVerification created = response.getData();

        assertThat(created.getId()).isEqualTo("78182ef2-8377-41cd-89e1-26e8266c9c94");
        assertThat(created.getCallbackUrl()).isEqualTo("http://example.com");
        assertThat(created.getCallbackMethod()).isEqualTo(CallbackMethod.GET);
        assertThat(created.getStatus()).isEqualTo(AddressVerificationStatus.PENDING);
    }
}
