package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class SupportingDocumentTemplateTest extends BaseTest {

    @Test
    void testListSupportingDocumentTemplates() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/supporting_document_templates"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("supporting_document_templates/index.json"))));

        ApiResponse<List<SupportingDocumentTemplate>> response = client.supportingDocumentTemplates().list();
        List<SupportingDocumentTemplate> templates = response.getData();

        assertThat(templates).isNotEmpty();
        assertThat(templates).hasSize(5);
        assertThat(templates.get(0).getId()).isEqualTo("206ccec2-1166-461f-9f58-3a56823db548");
        assertThat(templates.get(0).getName()).isEqualTo("Generic LOI");
        assertThat(templates.get(0).getPermanent()).isFalse();
    }
}
