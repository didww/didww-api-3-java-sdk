package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class CountryTest extends BaseTest {

    @Test
    void testListCountries() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/countries"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("countries/index.json"))));

        ApiResponse<List<Country>> response = client.countries().list();
        List<Country> countries = response.getData();

        assertThat(countries).isNotEmpty();

        Country first = countries.get(0);
        assertThat(first.getId()).isEqualTo("6c7727b3-6e17-4b8b-a4b3-4c5142e31a63");
        assertThat(first.getName()).isEqualTo("Afghanistan");
        assertThat(first.getPrefix()).isEqualTo("93");
        assertThat(first.getIso()).isEqualTo("AF");
    }

    @Test
    void testFindCountry() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/countries/7eda11bb-0e66-4146-98e7-57a5281f56c8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("countries/show.json"))));

        ApiResponse<Country> response = client.countries().find("7eda11bb-0e66-4146-98e7-57a5281f56c8");
        Country country = response.getData();

        assertThat(country.getName()).isEqualTo("United Kingdom");
        assertThat(country.getPrefix()).isEqualTo("44");
        assertThat(country.getIso()).isEqualTo("GB");
    }

    @Test
    void testFindCountryWithRegions() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/countries/661d8448-8897-4765-acda-00cc1740148d"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("countries/show_with_regions.json"))));

        QueryParams params = QueryParams.builder()
                .include("regions")
                .build();
        ApiResponse<Country> response = client.countries().find("661d8448-8897-4765-acda-00cc1740148d", params);
        Country country = response.getData();

        assertThat(country.getName()).isEqualTo("Lithuania");
        assertThat(country.getPrefix()).isEqualTo("370");
        assertThat(country.getIso()).isEqualTo("LT");

        List<Region> regions = country.getRegions();
        assertThat(regions).hasSize(10);
        assertThat(regions.get(0).getName()).isEqualTo("Alytaus Apskritis");
    }
}
