package com.didww.sdk.callback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidatorTest {

    private static Map<String, String> ordersPayload() {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("status", "completed");
        payload.put("id", "1dd7a68b-e235-402b-8912-fe73ee14243a");
        payload.put("type", "orders");
        return payload;
    }

    @Test
    void testSandbox() {
        RequestValidator validator = new RequestValidator("SOMEAPIKEY");
        String url = "http://example.com/callback.php?id=7ae7c48f-d48a-499f-9dc1-c9217014b457&reject_reason=&status=approved&type=address_verifications";
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("status", "approved");
        payload.put("id", "7ae7c48f-d48a-499f-9dc1-c9217014b457");
        payload.put("type", "address_verifications");
        payload.put("reject_reason", "");

        assertThat(validator.validate(url, payload, "18050028b6b22d0ed516706fba1c1af8d6a8f9d5")).isTrue();
    }

    @Test
    void testValidRequest() {
        RequestValidator validator = new RequestValidator("SOMEAPIKEY");

        assertThat(validator.validate("http://example.com/callbacks", ordersPayload(), "fe99e416c3547f2f59002403ec856ea386d05b2f")).isTrue();
    }

    @Test
    void testValidRequestWithQueryAndFragment() {
        RequestValidator validator = new RequestValidator("OTHERAPIKEY");

        assertThat(validator.validate("http://example.com/callbacks?foo=bar#baz", ordersPayload(), "32754ba93ac1207e540c0cf90371e7786b3b1cde")).isTrue();
    }

    @Test
    void testEmptySignatureRequest() {
        RequestValidator validator = new RequestValidator("SOMEAPIKEY");

        assertThat(validator.validate("http://example.com/callbacks", ordersPayload(), "")).isFalse();
    }

    @Test
    void testInvalidSignatureRequest() {
        RequestValidator validator = new RequestValidator("SOMEAPIKEY");

        assertThat(validator.validate("http://example.com/callbacks", ordersPayload(), "fbdb1d1b18aa6c08324b7d64b71fb76370690e1d")).isFalse();
    }

    static Stream<Arguments> urlNormalizationVectors() {
        return Stream.of(
            Arguments.of("http://foo.com/bar", "4d1ce2be656d20d064183bec2ab98a2ff3981f73"),
            Arguments.of("http://foo.com:80/bar", "4d1ce2be656d20d064183bec2ab98a2ff3981f73"),
            Arguments.of("http://foo.com:443/bar", "904eaa65c0759afac0e4d8912de424e2dfb96ea1"),
            Arguments.of("http://foo.com:8182/bar", "eb8fcfb3d7ed4b4c2265d73cf93c31ba614384d1"),
            Arguments.of("http://foo.com/bar?baz=boo", "78b00717a86ce9df06abf45ff818aa94537e1729"),
            Arguments.of("http://user:pass@foo.com/bar", "88615a11a78c021c1da2e1e0bfb8cc165170afc5"),
            Arguments.of("http://foo.com/bar#test", "b1c4391fcdab7c0521bb5b9eb4f41f08529b8418"),
            Arguments.of("https://foo.com/bar", "f26a771c302319a7094accbe2989bad67fff2928"),
            Arguments.of("https://foo.com:443/bar", "f26a771c302319a7094accbe2989bad67fff2928"),
            Arguments.of("https://foo.com:80/bar", "bd45af5253b72f6383c6af7dc75250f12b73a4e1"),
            Arguments.of("https://foo.com:8384/bar", "9c9fec4b7ebd6e1c461cb8e4ffe4f2987a19a5d3"),
            Arguments.of("https://foo.com/bar?qwe=asd", "4a0e98ddf286acadd1d5be1b0ed85a4e541c3137"),
            Arguments.of("https://qwe:asd@foo.com/bar", "7a8cd4a6c349910dfecaf9807e56a63787250bbd"),
            Arguments.of("https://foo.com/bar#baz", "5024919770ea5ca2e3ccc07cb940323d79819508"),
            Arguments.of("http://[::1]/bar", "e0e9b83e4046d097f54b3ae64b08cbb4a539f601"),
            Arguments.of("http://[::1]:80/bar", "e0e9b83e4046d097f54b3ae64b08cbb4a539f601"),
            Arguments.of("http://[::1]:9090/bar", "ebec110ec5debd0e0fd086ff2f02e48ca665b543"),
            Arguments.of("https://[::1]/bar", "f3cfe6f523fdf1d4eaadc310fcd3ed92e1e324b0"),
            Arguments.of("http://foo.com/hello%20world", "eb64035b2e8f356ff1442898a39ec94d5c3e2fc8"),
            Arguments.of("http://foo.com/foo%2Fbar", "db24428442b012fa0972a453ba1ba98e755bba10")
        );
    }

    @ParameterizedTest
    @MethodSource("urlNormalizationVectors")
    void testUrlNormalization(String url, String expectedSignature) {
        RequestValidator validator = new RequestValidator("SOMEAPIKEY");

        assertThat(validator.validate(url, ordersPayload(), expectedSignature)).isTrue();
    }
}
