package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.enums.ExportStatus;
import com.didww.sdk.resource.enums.ExportType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ExportTest extends BaseTest {

    @Test
    void testFindExport() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/exports/da15f006-5da4-45ca-b0df-735baeadf423"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("exports/show.json"))));

        ApiResponse<Export> response = client.exports().find("da15f006-5da4-45ca-b0df-735baeadf423");
        Export export = response.getData();

        assertThat(export.getId()).isEqualTo("da15f006-5da4-45ca-b0df-735baeadf423");
        assertThat(export.getStatus()).isEqualTo(ExportStatus.COMPLETED);
        assertThat(export.getExportType()).isEqualTo(ExportType.CDR_IN);
        assertThat(export.getUrl()).isEqualTo("https://sandbox-api.didww.com/v3/exports/e5352384-6f64-4132-bba1-cda18fbc5896.csv");
    }

    @Test
    void testCreateCdrOutExport() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/exports"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("exports/create_cdr_out.json"))));

        Export export = new Export();
        export.setExportType(ExportType.CDR_OUT);

        ApiResponse<Export> response = client.exports().create(export);
        Export created = response.getData();

        assertThat(created.getId()).isEqualTo("da15f006-5da4-45ca-b0df-735baeadf423");
        assertThat(created.getStatus()).isEqualTo(ExportStatus.PENDING);
        assertThat(created.getExportType()).isEqualTo(ExportType.CDR_OUT);
    }

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
        assertThat(first.getStatus()).isEqualTo(ExportStatus.COMPLETED);
        assertThat(first.getExportType()).isEqualTo(ExportType.CDR_IN);
    }

    @Test
    void testCreateExport() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/exports"))
                .withRequestBody(equalToJson(loadFixture("exports/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("exports/create.json"))));

        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("did_number", "1234556789");
        filters.put("year", "2019");
        filters.put("month", "01");

        Export export = new Export();
        export.setExportType(ExportType.CDR_IN);
        export.setFilters(filters);

        ApiResponse<Export> response = client.exports().create(export);
        Export created = response.getData();

        assertThat(created.getId()).isEqualTo("da15f006-5da4-45ca-b0df-735baeadf423");
        assertThat(created.getStatus()).isEqualTo(ExportStatus.PENDING);
        assertThat(created.getExportType()).isEqualTo(ExportType.CDR_IN);
    }
}
