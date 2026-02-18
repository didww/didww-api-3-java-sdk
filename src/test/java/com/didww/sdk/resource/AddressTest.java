package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class AddressTest extends BaseTest {

    @Test
    void testListAddresses() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/addresses"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("addresses/index.json"))));

        ApiResponse<List<Address>> response = client.addresses().list();
        List<Address> addresses = response.getData();

        assertThat(addresses).isNotEmpty();
        assertThat(addresses.get(0).getId()).isEqualTo("9d3f2582-a292-4b6b-828b-74c78b5a3780");
        assertThat(addresses.get(0).getCityName()).isEqualTo("Odessa");
        assertThat(addresses.get(0).getPostalCode()).isEqualTo("65000");
        assertThat(addresses.get(0).getAddress()).isEqualTo("literurna 12");
        assertThat(addresses.get(0).getDescription()).isEqualTo("1");
        assertThat(addresses.get(0).getVerified()).isFalse();
    }

    @Test
    void testCreateAddress() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/addresses"))
                .withRequestBody(equalToJson(loadFixture("addresses/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("addresses/create.json"))));

        Address address = new Address();
        address.setCityName("New York");
        address.setPostalCode("123");
        address.setAddress("some street");
        address.setDescription("test address");

        ApiResponse<Address> response = client.addresses().create(address);
        Address created = response.getData();

        assertThat(created.getId()).isEqualTo("bf69bc70-e1c2-442c-9f30-335ee299b663");
        assertThat(created.getCityName()).isEqualTo("New York");
        assertThat(created.getPostalCode()).isEqualTo("123");
        assertThat(created.getAddress()).isEqualTo("some street");
        assertThat(created.getDescription()).isEqualTo("test address");
        assertThat(created.getVerified()).isFalse();
    }
}
