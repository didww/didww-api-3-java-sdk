package com.didww.sdk.internal;

import okhttp3.HttpUrl;

/**
 * Internal helper, not part of the SDK API: service URL checks.
 */
public final class Urls {

    private Urls() {
    }

    /**
     * Returns whether {@code url} is an http or https URL with a host and without
     * a query, a fragment, whitespace or control characters, which would swallow
     * or change the paths appended to it.
     *
     * @param url the URL to check
     * @return true when it can be used as a service base URL
     */
    public static boolean isServiceUrl(String url) {
        if (url == null || url.indexOf('?') >= 0 || url.indexOf('#') >= 0) {
            return false;
        }
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c <= 0x20 || c == 0x7f) {
                return false;
            }
        }
        HttpUrl parsed = HttpUrl.parse(url);
        return parsed != null && !parsed.host().isEmpty();
    }

    /**
     * Removes one trailing {@code /} from {@code url}.
     *
     * @param url the URL
     * @return the URL without a trailing slash
     */
    public static String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Joins {@code base} and {@code path}, appending {@code /v3} to {@code base}
     * when it does not end with it.
     *
     * @param base the service base URL, with or without {@code /v3}
     * @param path the path below {@code /v3}
     * @return the joined URL
     */
    public static String joinV3(String base, String path) {
        String b = trimTrailingSlash(base);
        if (!b.endsWith("/v3")) {
            b += "/v3";
        }
        return b + "/" + (path.startsWith("/") ? path.substring(1) : path);
    }

    /**
     * Returns whether {@code a} and {@code b} have the same scheme, host name
     * (ignoring case and a trailing dot) and effective port.
     *
     * @param a one URL
     * @param b the other URL
     * @return true when both have the same origin
     */
    public static boolean sameOrigin(HttpUrl a, HttpUrl b) {
        return a.scheme().equalsIgnoreCase(b.scheme())
                && hostWithoutTrailingDot(a).equalsIgnoreCase(hostWithoutTrailingDot(b))
                && a.port() == b.port();
    }

    /**
     * Returns the {@code scheme://host:port} form of {@code url}.
     *
     * @param url the URL
     * @return its origin
     */
    public static String origin(HttpUrl url) {
        String host = url.host().indexOf(':') >= 0 ? "[" + url.host() + "]" : url.host();
        return url.scheme() + "://" + host + ":" + url.port();
    }

    private static String hostWithoutTrailingDot(HttpUrl url) {
        String host = url.host();
        return host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
    }
}
