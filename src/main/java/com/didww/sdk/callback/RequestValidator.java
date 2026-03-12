package com.didww.sdk.callback;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class RequestValidator {

    public static final String HEADER_NAME = "X-DIDWW-Signature";

    private final String apiKey;

    public RequestValidator(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean validate(String url, Map<String, String> payload, String signature) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        byte[] expectedBytes = computeHmac(url, payload);
        byte[] signatureBytes = hexToBytes(signature);
        if (signatureBytes.length == 0) {
            return false;
        }
        return MessageDigest.isEqual(expectedBytes, signatureBytes);
    }

    private byte[] computeHmac(String url, Map<String, String> payload) {
        TreeMap<String, String> sorted = new TreeMap<>(payload);
        StringBuilder data = new StringBuilder(normalizeUrl(url));
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            data.append(entry.getKey()).append(entry.getValue());
        }
        return hmacSha1Bytes(data.toString(), apiKey);
    }

    private String normalizeUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            String userInfo = uri.getUserInfo() != null ? uri.getUserInfo() + "@" : "";
            String host = uri.getHost();

            int port;
            if (uri.getPort() != -1) {
                port = uri.getPort();
            } else if ("https".equals(scheme)) {
                port = 443;
            } else {
                port = 80;
            }

            String path = uri.getRawPath();
            String query = uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";
            String fragment = uri.getRawFragment() != null ? "#" + uri.getRawFragment() : "";

            return scheme + "://" + userInfo + host + ":" + port + path + query + fragment;
        } catch (Exception e) {
            return "";
        }
    }

    private static byte[] hmacSha1Bytes(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA1", e); // NOSONAR
        }
    }

    private static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int hi = Character.digit(hex.charAt(i * 2), 16);
            int lo = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (hi == -1 || lo == -1) {
                return new byte[0];
            }
            bytes[i] = (byte) ((hi << 4) | lo);
        }
        return bytes;
    }
}
