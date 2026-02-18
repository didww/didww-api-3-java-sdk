package com.didww.sdk.callback;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
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
        return validSignature(url, payload).equals(signature);
    }

    private String validSignature(String url, Map<String, String> payload) {
        TreeMap<String, String> sorted = new TreeMap<>(payload);
        StringBuilder data = new StringBuilder(normalizeUrl(url));
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            data.append(entry.getKey()).append(entry.getValue());
        }
        return hmacSha1(data.toString(), apiKey);
    }

    private String normalizeUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
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

            String path = uri.getRawPath() != null ? uri.getRawPath() : "";
            String query = uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";
            String fragment = uri.getRawFragment() != null ? "#" + uri.getRawFragment() : "";

            return scheme + "://" + userInfo + host + ":" + port + path + query + fragment;
        } catch (Exception e) {
            return "";
        }
    }

    private static String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA1"));
            byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA1", e);
        }
    }
}
