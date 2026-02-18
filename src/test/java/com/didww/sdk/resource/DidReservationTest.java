package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.resource.*;
import com.didww.sdk.repository.ApiResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class DidReservationTest extends BaseTest {

    @Test
    void testListDidReservations() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/did_reservations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_reservations/index.json"))));

        ApiResponse<List<DidReservation>> response = client.didReservations().list();
        List<DidReservation> didReservations = response.getData();

        assertThat(didReservations).isNotEmpty();
    }

    @Test
    void testFindDidReservation() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/did_reservations/fd38d3ff-80cf-4e67-a605-609a2884a5c4"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_reservations/show.json"))));

        ApiResponse<DidReservation> response = client.didReservations().find("fd38d3ff-80cf-4e67-a605-609a2884a5c4");
        DidReservation didReservation = response.getData();

        assertThat(didReservation.getDescription()).isEqualTo("DIDWW");
    }

    @Test
    void testCreateDidReservation() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/did_reservations"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_reservations/create.json"))));

        DidReservation newReservation = new DidReservation();
        newReservation.setDescription("DIDWW");

        ApiResponse<DidReservation> response = client.didReservations().create(newReservation);
        DidReservation didReservation = response.getData();

        assertThat(didReservation.getId()).isEqualTo("fd38d3ff-80cf-4e67-a605-609a2884a5c4");
        assertThat(didReservation.getDescription()).isEqualTo("DIDWW");
    }
}
