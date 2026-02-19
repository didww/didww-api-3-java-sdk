package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.IdentityType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class IdentityTest extends BaseTest {

    @Test
    void testListIdentities() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/identities"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("identities/index.json"))));

        ApiResponse<List<Identity>> response = client.identities().list();
        List<Identity> identities = response.getData();

        assertThat(identities).isNotEmpty();
        assertThat(identities).hasSize(2);
        assertThat(identities.get(0).getId()).isEqualTo("5e9df058-50d2-4e34-b0d4-d1746b86f41a");
        assertThat(identities.get(0).getFirstName()).isEqualTo("John");
        assertThat(identities.get(0).getLastName()).isEqualTo("Doe");
        assertThat(identities.get(0).getIdentityType()).isEqualTo(IdentityType.PERSONAL);
    }

    @Test
    void testCreateIdentity() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/identities"))
                .withRequestBody(equalToJson(loadFixture("identities/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("identities/create.json"))));

        Country country = new Country();
        country.setId("1f6fc2bd-f081-4202-9b1a-d9cb88d942b9");

        Identity identity = new Identity();
        identity.setFirstName("John");
        identity.setLastName("Doe");
        identity.setPhoneNumber("123456789");
        identity.setIdNumber("ABC1234");
        identity.setBirthDate(LocalDate.of(1970, 1, 1));
        identity.setCompanyName("Test Company Limited");
        identity.setCompanyRegNumber("543221");
        identity.setVatId("GB1234");
        identity.setDescription("test identity");
        identity.setPersonalTaxId("987654321");
        identity.setIdentityType(IdentityType.BUSINESS);
        identity.setExternalReferenceId("111");
        identity.setCountry(country);

        ApiResponse<Identity> response = client.identities().create(identity);
        Identity created = response.getData();

        assertThat(created.getId()).isEqualTo("e96ae7d1-11d5-42bc-a5c5-211f3c3788ae");
        assertThat(created.getFirstName()).isEqualTo("John");
        assertThat(created.getIdentityType()).isEqualTo(IdentityType.BUSINESS);
        assertThat(created.getVerified()).isFalse();
    }

    @Test
    void testDeleteIdentity() {
        String id = "e96ae7d1-11d5-42bc-a5c5-211f3c3788ae";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/identities/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.identities().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/identities/" + id)));
    }
}
