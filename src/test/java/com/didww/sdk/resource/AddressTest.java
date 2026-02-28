package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
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

        QueryParams params = QueryParams.builder()
                .include("country", "identity", "proofs", "area", "city")
                .build();
        ApiResponse<List<Address>> response = client.addresses().list(params);
        List<Address> addresses = response.getData();

        assertThat(addresses).isNotEmpty();
        assertThat(addresses.get(0).getId()).isEqualTo("9d3f2582-a292-4b6b-828b-74c78b5a3780");
        assertThat(addresses.get(0).getCityName()).isEqualTo("Odessa");
        assertThat(addresses.get(0).getPostalCode()).isEqualTo("65000");
        assertThat(addresses.get(0).getAddress()).isEqualTo("literurna 12");
        assertThat(addresses.get(0).getDescription()).isEqualTo("1");
        assertThat(addresses.get(0).getVerified()).isFalse();
        assertThat(addresses.get(0).getCountry()).isNotNull();
        assertThat(addresses.get(0).getCountry().getName()).isEqualTo("Ukraine");
        assertThat(addresses.get(0).getCountry().getIso()).isEqualTo("UA");

        assertThat(addresses.get(0).getIdentity()).isNotNull();
        assertThat(addresses.get(0).getIdentity().getFirstName()).isEqualTo("John");
        assertThat(addresses.get(0).getIdentity().getLastName()).isEqualTo("Doe");

        assertThat(addresses.get(0).getProofs()).hasSize(1);
        assertThat(addresses.get(0).getProofs().get(0).getId()).isEqualTo("dd2f5f37-0d08-415d-9530-6488e6eb797b");

        assertThat(addresses.get(0).getArea()).isNull();
        assertThat(addresses.get(0).getCity()).isNull();
    }

    @Test
    void testCreateAddress() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/addresses"))
                .withRequestBody(equalToJson(loadFixture("addresses/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("addresses/create.json"))));

        Country country = Country.build("1f6fc2bd-f081-4202-9b1a-d9cb88d942b9");
        Identity identity = Identity.build("5e9df058-50d2-4e34-b0d4-d1746b86f41a");

        Address address = new Address();
        address.setCityName("New York");
        address.setPostalCode("123");
        address.setAddress("some street");
        address.setDescription("test address");
        address.setCountry(country);
        address.setIdentity(identity);

        QueryParams createParams = QueryParams.builder()
                .include("country")
                .build();
        ApiResponse<Address> response = client.addresses().create(address, createParams);
        Address created = response.getData();

        assertThat(created.getId()).isEqualTo("bf69bc70-e1c2-442c-9f30-335ee299b663");
        assertThat(created.getCityName()).isEqualTo("New York");
        assertThat(created.getPostalCode()).isEqualTo("123");
        assertThat(created.getAddress()).isEqualTo("some street");
        assertThat(created.getDescription()).isEqualTo("test address");
        assertThat(created.getVerified()).isFalse();
        assertThat(created.getCountry()).isNotNull();
        assertThat(created.getCountry().getName()).isEqualTo("United States");
        assertThat(created.getCountry().getIso()).isEqualTo("US");
    }

    @Test
    void testUpdateAddress() {
        wireMock.stubFor(patch(urlPathEqualTo("/v3/addresses/bf69bc70-e1c2-442c-9f30-335ee299b663"))
                .withRequestBody(equalToJson(loadFixture("addresses/update_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("addresses/update.json"))));

        Address address = Address.build("bf69bc70-e1c2-442c-9f30-335ee299b663");
        address.setCityName("Chicago");
        address.setPostalCode("1234");
        address.setAddress("Main street");
        address.setDescription("some address");

        ApiResponse<Address> response = client.addresses().update(address);
        Address updated = response.getData();

        assertThat(updated.getId()).isEqualTo("bf69bc70-e1c2-442c-9f30-335ee299b663");
        assertThat(updated.getCityName()).isEqualTo("Chicago");
        assertThat(updated.getPostalCode()).isEqualTo("1234");
        assertThat(updated.getAddress()).isEqualTo("Main street");
        assertThat(updated.getDescription()).isEqualTo("some address");
        assertThat(updated.getVerified()).isFalse();
    }

    @Test
    void testDeleteAddress() {
        String id = "bf69bc70-e1c2-442c-9f30-335ee299b663";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/addresses/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.addresses().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/addresses/" + id)));
    }
}
