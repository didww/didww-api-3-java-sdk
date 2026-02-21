package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

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

        QueryParams params = QueryParams.builder()
                .include("available_did.did_group.stock_keeping_units")
                .build();
        ApiResponse<DidReservation> response = client.didReservations().find("fd38d3ff-80cf-4e67-a605-609a2884a5c4", params);
        DidReservation didReservation = response.getData();

        assertThat(didReservation.getDescription()).isEqualTo("DIDWW");
        assertThat(didReservation.getAvailableDid()).isNotNull();
    }

    @Test
    void testCreateDidReservation() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/did_reservations"))
                .withRequestBody(equalToJson(loadFixture("did_reservations/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("did_reservations/create.json"))));

        AvailableDid availableDid = AvailableDid.build("857d1462-5f43-4238-b007-ff05f282e41b");

        DidReservation newReservation = new DidReservation();
        newReservation.setDescription("DIDWW");
        newReservation.setAvailableDid(availableDid);

        ApiResponse<DidReservation> response = client.didReservations().create(newReservation);
        DidReservation didReservation = response.getData();

        assertThat(didReservation.getId()).isEqualTo("fd38d3ff-80cf-4e67-a605-609a2884a5c4");
        assertThat(didReservation.getDescription()).isEqualTo("DIDWW");
    }

    @Test
    void testDeleteDidReservation() {
        String id = "fd38d3ff-80cf-4e67-a605-609a2884a5c4";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/did_reservations/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.didReservations().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/did_reservations/" + id)));
    }
}
