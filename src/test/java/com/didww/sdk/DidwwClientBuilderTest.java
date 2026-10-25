package com.didww.sdk;

import com.didww.sdk.auth.DidwwScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DidwwClientBuilderTest {

    private static DidwwClient.Builder builder(String apiKey) {
        return DidwwClient.builder().credentials(new DidwwCredentials(apiKey, DidwwEnvironment.SANDBOX));
    }

    @Test
    void testEmptyApiKeyRejected() {
        assertThatThrownBy(() -> builder("").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("API key is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"key\nwith-newline", "key\rx", "s\u00e9cretKEY", "key\u007f", "key\ttab", "key\u0000"})
    void testApiKeyWithCharactersNotAllowedInHeaderRejectedWithoutEchoingIt(String apiKey) {
        assertThatThrownBy(() -> builder(apiKey).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("API key contains characters not allowed in an HTTP header")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain(apiKey));
    }

    @Test
    void testPrintableAsciiApiKeyAccepted() {
        assertThatCode(() -> builder("Abc-123_~!@#$%^&*()+= x").build()).doesNotThrowAnyException();
    }

    @Test
    void testDefaultApiVersion() {
        DidwwClient client = builder("key").build();

        assertThat(DidwwClient.DEFAULT_API_VERSION).isEqualTo("2026-10-22");
        assertThat(client.getApiVersion()).isEqualTo("2026-10-22");
        assertThat(client.getScope()).isNull();
    }

    @Test
    void testLaterApiVersionAccepted() {
        DidwwClient client = builder("key").apiVersion("2099-01-01").build();

        assertThat(client.getApiVersion()).isEqualTo("2099-01-01");
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-4-16", "abc", "2026-10-22\n", "2026-10-22 ", " 2026-10-22", "2026/10/22",
            "20261022", "\u0662\u0660\u0662\u0666-10-22", ""})
    void testMalformedApiVersionRejected(String version) {
        assertThatThrownBy(() -> builder("key").apiVersion(version).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("API version must be a YYYY-MM-DD date");
    }

    @Test
    void testEarlierApiVersionRejectedWithPointerToTheOlderSdk() {
        assertThatThrownBy(() -> builder("key").apiVersion("2026-04-16").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("API version 2026-04-16 is not supported: this SDK requires 2026-10-22 or later; "
                        + "use didww-api-3-java-sdk 4.x (branch release-4.1) for 2026-04-16");
    }

    @Test
    void testScope() {
        assertThat(builder("key").scope(DidwwScope.READ).build().getScope()).isEqualTo(DidwwScope.READ);
        assertThat(DidwwScope.READ.getValue()).isEqualTo("read");
        assertThat(DidwwScope.FULL.getValue()).isEqualTo("full");
    }

    @Test
    void testEnvironmentEndpoints() {
        DidwwClient sandbox = builder("key").build();
        assertThat(sandbox.getBaseUrl()).isEqualTo("https://sandbox-api.didww.com/v3");
        assertThat(sandbox.getAuthUrl()).isEqualTo("https://auth-sandbox.didww.com");
        assertThat(sandbox.getVoiceInPricesUrl()).isEqualTo("https://api-vin-sandbox.didww.com/v3");
        assertThat(sandbox.getVoiceOutPricesUrl()).isEqualTo("https://api-vout-sandbox.didww.com/v3");
        assertThat(sandbox.getSmsPricesUrl()).isEqualTo("https://api-sms-sandbox.didww.com/v3");

        DidwwClient production = DidwwClient.builder()
                .credentials(new DidwwCredentials("key", DidwwEnvironment.PRODUCTION))
                .build();
        assertThat(production.getBaseUrl()).isEqualTo("https://api.didww.com/v3");
        assertThat(production.getAuthUrl()).isEqualTo("https://auth.didww.com");
        assertThat(production.getVoiceInPricesUrl()).isEqualTo("https://api-vin.didww.com/v3");
        assertThat(production.getVoiceOutPricesUrl()).isEqualTo("https://api-vout.didww.com/v3");
        assertThat(production.getSmsPricesUrl()).isEqualTo("https://api-sms.didww.com/v3");
    }

    @Test
    void testOverriddenEndpoints() {
        DidwwClient client = builder("key")
                .baseUrl("https://proxy.example.com/v3/")
                .authUrl("https://auth.example.com/")
                .voiceInPricesUrl("https://vin.example.com")
                .voiceOutPricesUrl("https://vout.example.com/v3")
                .smsPricesUrl("http://sms.example.com:8080")
                .build();

        assertThat(client.getBaseUrl()).isEqualTo("https://proxy.example.com/v3");
        assertThat(client.getAuthUrl()).isEqualTo("https://auth.example.com");
        assertThat(client.getVoiceInPricesUrl()).isEqualTo("https://vin.example.com");
        assertThat(client.getVoiceOutPricesUrl()).isEqualTo("https://vout.example.com/v3");
        assertThat(client.getSmsPricesUrl()).isEqualTo("http://sms.example.com:8080");
    }

    @Test
    void testEveryServiceUrlIsChecked() {
        Map<String, BiFunction<DidwwClient.Builder, String, DidwwClient.Builder>> setters = new LinkedHashMap<>();
        setters.put("API base", DidwwClient.Builder::baseUrl);
        setters.put("Token service", DidwwClient.Builder::authUrl);
        setters.put("Voice IN prices", DidwwClient.Builder::voiceInPricesUrl);
        setters.put("Voice OUT prices", DidwwClient.Builder::voiceOutPricesUrl);
        setters.put("SMS prices", DidwwClient.Builder::smsPricesUrl);
        List<String> invalid = List.of("ftp://example.com", "http://", "example.com/v3", "https://h/v3?x=1",
                "https://h/v3#f", "https://h/v3 x", "https://h/v3\n", "mailto:a@b.c", "");

        setters.forEach((name, setter) -> {
            for (String url : invalid) {
                assertThatThrownBy(() -> setter.apply(builder("key"), url).build())
                        .as("%s URL %s", name, url)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageStartingWith(name + " URL must be an http or https URL without a query or fragment");
            }
        });
    }

    @Test
    void testServiceUrlWithUserInfoIsNotEchoed() {
        assertThatThrownBy(() -> builder("key").authUrl("https://user:secret@auth.example.com/x?y").build())
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("secret"));
    }

    @Test
    void testUrlValidationRunsAfterApiKeyCheck() {
        assertThatThrownBy(() -> builder("bad\nkey").baseUrl("ftp://x").build())
                .hasMessage("API key contains characters not allowed in an HTTP header");
    }
}
