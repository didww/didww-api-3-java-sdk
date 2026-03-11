package com.didww.sdk.exception;

import com.didww.sdk.BaseTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DidwwApiExceptionTest extends BaseTest {

    @Test
    void testErrorWithDetailField() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/dids"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody("{\"errors\":[{\"title\":\"Invalid attribute\",\"detail\":\"Name is too short\",\"status\":\"422\"}]}")));

        assertThatThrownBy(() -> client.dids().list())
                .isInstanceOf(DidwwApiException.class)
                .satisfies(ex -> {
                    DidwwApiException apiEx = (DidwwApiException) ex;
                    assertThat(apiEx.getHttpStatus()).isEqualTo(422);
                    assertThat(apiEx.getErrors()).hasSize(1);
                    assertThat(apiEx.getErrors().get(0).getDetail()).isEqualTo("Name is too short");
                    assertThat(apiEx.getErrors().get(0).getTitle()).isEqualTo("Invalid attribute");
                    assertThat(apiEx.getErrors().get(0).getMessage()).isEqualTo("Name is too short");
                    assertThat(apiEx.getMessage()).contains("Name is too short");
                });
    }

    @Test
    void testErrorWithTitleOnlyFallback() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/dids"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody("{\"errors\":[{\"title\":\"Forbidden\",\"status\":\"403\"}]}")));

        assertThatThrownBy(() -> client.dids().list())
                .isInstanceOf(DidwwApiException.class)
                .satisfies(ex -> {
                    DidwwApiException apiEx = (DidwwApiException) ex;
                    assertThat(apiEx.getHttpStatus()).isEqualTo(403);
                    assertThat(apiEx.getErrors()).hasSize(1);
                    assertThat(apiEx.getErrors().get(0).getDetail()).isNull();
                    assertThat(apiEx.getErrors().get(0).getTitle()).isEqualTo("Forbidden");
                    assertThat(apiEx.getErrors().get(0).getMessage()).isEqualTo("Forbidden");
                    assertThat(apiEx.getMessage()).contains("Forbidden");
                });
    }

    @Test
    void testErrorWithNoErrorsArray() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/dids"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody("{\"message\":\"Internal Server Error\"}")));

        assertThatThrownBy(() -> client.dids().list())
                .isInstanceOf(DidwwApiException.class)
                .satisfies(ex -> {
                    DidwwApiException apiEx = (DidwwApiException) ex;
                    assertThat(apiEx.getHttpStatus()).isEqualTo(500);
                    assertThat(apiEx.getErrors()).isEmpty();
                });
    }

    @Test
    void testApiErrorGetMessageReturnsDetailOverTitle() {
        DidwwApiException.ApiError error = new DidwwApiException.ApiError();
        error.setTitle("Some Title");
        error.setDetail("Some Detail");
        assertThat(error.getMessage()).isEqualTo("Some Detail");
    }

    @Test
    void testApiErrorGetMessageFallsBackToTitle() {
        DidwwApiException.ApiError error = new DidwwApiException.ApiError();
        error.setTitle("Some Title");
        assertThat(error.getMessage()).isEqualTo("Some Title");
    }

    @Test
    void testApiErrorGetMessageReturnsFallbackWhenBothMissing() {
        DidwwApiException.ApiError error = new DidwwApiException.ApiError();
        assertThat(error.getMessage()).isEqualTo("Unknown error");
    }
}
