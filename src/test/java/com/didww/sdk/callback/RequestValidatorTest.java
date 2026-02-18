package com.didww.sdk.callback;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidatorTest {

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
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("status", "completed");
        payload.put("id", "1dd7a68b-e235-402b-8912-fe73ee14243a");
        payload.put("type", "orders");

        assertThat(validator.validate("http://example.com/callbacks", payload, "fe99e416c3547f2f59002403ec856ea386d05b2f")).isTrue();
    }

    @Test
    void testValidRequestWithQueryAndFragment() {
        RequestValidator validator = new RequestValidator("OTHERAPIKEY");
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("status", "completed");
        payload.put("id", "1dd7a68b-e235-402b-8912-fe73ee14243a");
        payload.put("type", "orders");

        assertThat(validator.validate("http://example.com/callbacks?foo=bar#baz", payload, "32754ba93ac1207e540c0cf90371e7786b3b1cde")).isTrue();
    }

    @Test
    void testEmptySignatureRequest() {
        RequestValidator validator = new RequestValidator("SOMEAPIKEY");
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("status", "completed");
        payload.put("id", "1dd7a68b-e235-402b-8912-fe73ee14243a");
        payload.put("type", "orders");

        assertThat(validator.validate("http://example.com/callbacks", payload, "")).isFalse();
    }

    @Test
    void testInvalidSignatureRequest() {
        RequestValidator validator = new RequestValidator("SOMEAPIKEY");
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("status", "completed");
        payload.put("id", "1dd7a68b-e235-402b-8912-fe73ee14243a");
        payload.put("type", "orders");

        assertThat(validator.validate("http://example.com/callbacks", payload, "fbdb1d1b18aa6c08324b7d64b71fb76370690e1d")).isFalse();
    }
}
