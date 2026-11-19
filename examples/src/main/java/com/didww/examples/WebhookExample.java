package com.didww.examples;

import com.didww.sdk.callback.RequestValidator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Validates the signature of a DIDWW callback with RequestValidator. No request is sent.
 *
 * DIDWW signs every callback with the callback secret enabled in the User Panel (APIs, DIDWW API 3,
 * Callback Secrets) and sends the signature in the X-DIDWW-Signature header
 * (RequestValidator.HEADER_NAME). The signature is the lowercase hex HMAC-SHA1, keyed with the secret, of:
 *   - the callback URL with the port written out (https://example.com:443/didww/callbacks), and
 *   - the payload keys and values, sorted by key and appended as key + value with no separator.
 *
 * This example builds a sample callback, signs it that way, and shows that the validator accepts the
 * signature and refuses it once the payload has been changed.
 *
 * Usage: ./gradlew runExample -PexampleClass=com.didww.examples.WebhookExample
 */
public class WebhookExample {

    public static void main(String[] args) {
        String callbackSecret = "your-callback-secret";
        RequestValidator validator = new RequestValidator(callbackSecret);

        // A callback as your endpoint receives it: the full URL it was sent to and the payload fields
        String url = "https://example.com/didww/callbacks?source=didww";
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("id", "1dd7a68b-e235-402b-8912-fe73ee14243a");
        payload.put("status", "completed");
        payload.put("type", "orders");

        // The value DIDWW sends in the X-DIDWW-Signature header
        String signature = sign(callbackSecret, url, payload);
        System.out.println("URL: " + url);
        System.out.println("Payload: " + payload);
        System.out.println(RequestValidator.HEADER_NAME + ": " + signature);

        boolean valid = validator.validate(url, payload, signature);
        System.out.println("valid: " + valid);

        // The same signature with a changed payload
        Map<String, String> tampered = new LinkedHashMap<>(payload);
        tampered.put("status", "canceled");
        boolean tamperedValid = validator.validate(url, tampered, signature);
        System.out.println("Tampered payload: " + tampered);
        System.out.println("valid: " + tamperedValid);

        if (!valid || tamperedValid) {
            throw new IllegalStateException("Unexpected validation result");
        }
    }

    /**
     * Signs a callback the way DIDWW does and RequestValidator checks it.
     *
     * @param secret  the callback secret
     * @param url     the full callback URL
     * @param payload the callback payload fields
     * @return the lowercase hex HMAC-SHA1 signature
     */
    static String sign(String secret, String url, Map<String, String> payload) {
        StringBuilder data = new StringBuilder(normalizeUrl(url));
        for (Map.Entry<String, String> field : new TreeMap<>(payload).entrySet()) {
            data.append(field.getKey()).append(field.getValue());
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] digest = mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA1 is not available", e);
        }
    }

    // scheme://[user-info@]host:port/path[?query][#fragment], with 443 or 80 when the URL has no port
    private static String normalizeUrl(String url) {
        URI uri = URI.create(url);
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        String userInfo = uri.getUserInfo() != null ? uri.getUserInfo() + "@" : "";
        int port = uri.getPort() != -1 ? uri.getPort() : ("https".equals(scheme) ? 443 : 80);
        String query = uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";
        String fragment = uri.getRawFragment() != null ? "#" + uri.getRawFragment() : "";
        return scheme + "://" + userInfo + uri.getHost() + ":" + port + uri.getRawPath() + query + fragment;
    }
}
