package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class PermanentSupportingDocumentTest extends BaseTest {

    @Test
    void testCreatePermanentSupportingDocument() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/permanent_supporting_documents"))
                .withRequestBody(equalToJson(loadFixture("permanent_supporting_documents/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("permanent_supporting_documents/create.json"))));

        Identity identity = Identity.build("5e9df058-50d2-4e34-b0d4-d1746b86f41a");
        SupportingDocumentTemplate template = SupportingDocumentTemplate.build("4199435f-646e-4e9d-a143-8f3b972b10c5");
        EncryptedFile encryptedFile = EncryptedFile.build("254b3c2d-c40c-4ff7-93b1-a677aee7fa10");

        PermanentSupportingDocument doc = new PermanentSupportingDocument();
        doc.setIdentity(identity);
        doc.setTemplate(template);
        doc.setFiles(Collections.singletonList(encryptedFile));

        QueryParams createParams = QueryParams.builder()
                .include("template")
                .build();
        ApiResponse<PermanentSupportingDocument> response = client.permanentSupportingDocuments().create(doc, createParams);
        PermanentSupportingDocument created = response.getData();

        assertThat(created.getId()).isEqualTo("19510da3-c07e-4fa9-a696-6b9ab89cc172");
        assertThat(created.getTemplate()).isNotNull();
        assertThat(created.getTemplate().getId()).isEqualTo("4199435f-646e-4e9d-a143-8f3b972b10c5");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void testDeletePermanentSupportingDocument() {
        String id = "19510da3-c07e-4fa9-a696-6b9ab89cc172";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/permanent_supporting_documents/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.permanentSupportingDocuments().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/permanent_supporting_documents/" + id)));
    }
}
