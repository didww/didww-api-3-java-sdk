package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.exception.DidwwApiException;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsTrunkRegenerateCredentialTest extends BaseTest {

    private static final String HTTP_OUT_ID = "1a2b3c4d-0003-4a5b-8c6d-7e8f9a0b1c03";

    @Test
    void testCreateSmsTrunkRegenerateCredential() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/sms_trunk_regenerate_credentials"))
                .withRequestBody(equalToJson(loadFixture("sms_trunk_regenerate_credentials/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunk_regenerate_credentials/create.json"))));

        SmsTrunkRegenerateCredential credential = new SmsTrunkRegenerateCredential();
        credential.setSmsTrunk(new SmsTrunk().withId(HTTP_OUT_ID));

        SmsTrunkRegenerateCredential created = client.smsTrunkRegenerateCredentials().create(credential).getData();

        assertThat(created.getId()).isEqualTo("3c4d5e6f-0001-4c7d-8e9f-0a1b2c3d4e01");
    }

    @Test
    void testCreateSmsTrunkRegenerateCredentialError() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/sms_trunk_regenerate_credentials"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("sms_trunk_regenerate_credentials/create_error.json"))));

        SmsTrunkRegenerateCredential credential = new SmsTrunkRegenerateCredential();
        credential.setSmsTrunk(new SmsTrunk().withId("1a2b3c4d-0002-4a5b-8c6d-7e8f9a0b1c02"));

        assertThatThrownBy(() -> client.smsTrunkRegenerateCredentials().create(credential))
                .isInstanceOf(DidwwApiException.class)
                .satisfies(thrown -> {
                    DidwwApiException ex = (DidwwApiException) thrown;
                    assertThat(ex.getHttpStatus()).isEqualTo(422);
                    assertThat(ex.getErrors().get(0).getDetail()).isEqualTo("sms_trunk - the trunk has no credentials to rotate");
                    assertThat(ex.getErrors().get(0).getSource()).containsEntry("pointer", "/data/relationships/sms_trunk");
                });
    }
}
