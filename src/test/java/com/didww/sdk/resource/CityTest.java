package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class CityTest extends BaseTest {

    @Test
    void testListCities() {
        stubGetFixture("/v3/cities", "cities/index.json");

        ApiResponse<List<City>> response = client.cities().list();
        List<City> cities = response.getData();

        assertThat(cities).isNotEmpty();
    }

    @Test
    void testFindCity() {
        stubGetFixture("/v3/cities/368bf92f-c36e-473f-96fc-d53ed1b4028b", "cities/show.json");

        QueryParams params = QueryParams.builder()
                .include("country", "region", "area")
                .build();
        ApiResponse<City> response = client.cities().find("368bf92f-c36e-473f-96fc-d53ed1b4028b", params);
        City city = response.getData();

        assertThat(city.getName()).isEqualTo("New York");
        assertThat(city.getCountry()).isNotNull();
        assertThat(city.getCountry().getName()).isEqualTo("United States");
        assertThat(city.getRegion()).isNotNull();
        assertThat(city.getRegion().getName()).isEqualTo("New York");
        assertThat(city.getArea()).isNull();
    }
}
