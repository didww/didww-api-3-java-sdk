package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class CityTest extends BaseTest {

    @Test
    void testListCities() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/cities"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("cities/index.json"))));

        ApiResponse<List<City>> response = client.cities().list();
        List<City> cities = response.getData();

        assertThat(cities).isNotEmpty();
    }

    @Test
    void testFindCity() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/cities/368bf92f-c36e-473f-96fc-d53ed1b4028b"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("cities/show.json"))));

        ApiResponse<City> response = client.cities().find("368bf92f-c36e-473f-96fc-d53ed1b4028b");
        City city = response.getData();

        assertThat(city.getName()).isEqualTo("New York");
    }
}
