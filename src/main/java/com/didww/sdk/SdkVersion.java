package com.didww.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides the SDK version, read from the build-generated
 * {@code didww-sdk-version.properties} resource.
 */
public final class SdkVersion {

    private static final String FALLBACK = "unknown";
    private static final String VERSION;

    static {
        String v = FALLBACK;
        try (InputStream is = SdkVersion.class.getClassLoader()
                .getResourceAsStream("didww-sdk-version.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String val = props.getProperty("version");
                if (val != null) {
                    val = val.trim();
                    if (!val.isEmpty()) {
                        v = val;
                    }
                }
            }
        } catch (IOException ignored) {
            // keep fallback
        }
        VERSION = v;
    }

    private SdkVersion() {
    }

    /**
     * Returns the SDK version string (e.g. {@code "1.0.0"}).
     * Falls back to {@code "unknown"} when the properties file is absent.
     */
    public static String get() {
        return VERSION;
    }

    /**
     * Returns the full User-Agent value, e.g. {@code "didww-java-sdk/1.0.0"}.
     */
    public static String userAgent() {
        return "didww-java-sdk/" + VERSION;
    }
}
