package com.didww.sdk.prices;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

// The file name of a Content-Disposition header: filename* (RFC 8187,
// UTF-8 or ISO-8859-1) when present and valid, else filename.
final class ContentDisposition {

    private ContentDisposition() {
    }

    static String fileName(String header) {
        if (header == null) {
            return null;
        }
        String plain = null;
        String extended = null;
        int i = header.indexOf(';');
        while (i >= 0 && i < header.length()) {
            int start = i + 1;
            int eq = header.indexOf('=', start);
            if (eq < 0) {
                break;
            }
            String name = header.substring(start, eq).trim().toLowerCase(Locale.ROOT);
            int pos = eq + 1;
            while (pos < header.length() && header.charAt(pos) == ' ') {
                pos++;
            }
            String value;
            if (pos < header.length() && header.charAt(pos) == '"') {
                StringBuilder sb = new StringBuilder();
                pos++;
                while (pos < header.length() && header.charAt(pos) != '"') {
                    char c = header.charAt(pos);
                    if (c == '\\' && pos + 1 < header.length()) {
                        c = header.charAt(++pos);
                    }
                    sb.append(c);
                    pos++;
                }
                value = sb.toString();
                i = header.indexOf(';', pos);
            } else {
                int end = header.indexOf(';', pos);
                value = (end < 0 ? header.substring(pos) : header.substring(pos, end)).trim();
                i = end;
            }
            if (name.equals("filename") && plain == null) {
                plain = value;
            } else if (name.equals("filename*") && extended == null) {
                extended = decodeExtended(value);
            }
        }
        String name = extended != null ? extended : plain;
        return name == null || name.isEmpty() ? null : name;
    }

    private static String decodeExtended(String value) {
        int first = value.indexOf('\'');
        int second = first < 0 ? -1 : value.indexOf('\'', first + 1);
        if (second < 0) {
            return null;
        }
        String charsetName = value.substring(0, first);
        Charset charset;
        if (charsetName.equalsIgnoreCase("UTF-8")) {
            charset = StandardCharsets.UTF_8;
        } else if (charsetName.equalsIgnoreCase("ISO-8859-1")) {
            charset = StandardCharsets.ISO_8859_1;
        } else {
            return null;
        }
        String encoded = value.substring(second + 1);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int p = 0; p < encoded.length(); p++) {
            char c = encoded.charAt(p);
            if (c == '%') {
                if (p + 2 >= encoded.length()) {
                    return null;
                }
                int hi = Character.digit(encoded.charAt(p + 1), 16);
                int lo = Character.digit(encoded.charAt(p + 2), 16);
                if (hi < 0 || lo < 0) {
                    return null;
                }
                bytes.write((hi << 4) | lo);
                p += 2;
            } else if (c > 0x20 && c < 0x7f) {
                bytes.write(c);
            } else {
                return null;
            }
        }
        return new String(bytes.toByteArray(), charset);
    }
}
