package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ExportTest extends BaseTest {

    @Test
    void testListExports() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/exports"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("exports/index.json"))));

        ApiResponse<List<Export>> response = client.exports().list();
        List<Export> exports = response.getData();

        assertThat(exports).isNotEmpty();

        Export first = exports.get(0);
        assertThat(first.getId()).isEqualTo("da15f006-5da4-45ca-b0df-735baeadf423");
        assertThat(first.getStatus()).isEqualTo("Completed");
        assertThat(first.getExportType()).isEqualTo("cdr_in");
    }

    @Test
    void testCreateExport() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/exports"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("exports/create.json"))));

        Export export = new Export();
        export.setExportType("cdr_in");

        ApiResponse<Export> response = client.exports().create(export);
        Export created = response.getData();

        assertThat(created.getId()).isEqualTo("da15f006-5da4-45ca-b0df-735baeadf423");
        assertThat(created.getStatus()).isEqualTo("Pending");
        assertThat(created.getExportType()).isEqualTo("cdr_in");
    }
}
