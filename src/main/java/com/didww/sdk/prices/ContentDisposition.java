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
            int eq = header.indexOf('=', i + 1);
            if (eq < 0) {
                break;
            }
            String name = header.substring(i + 1, eq).trim().toLowerCase(Locale.ROOT);
            StringBuilder value = new StringBuilder();
            i = readValue(header, skipSpaces(header, eq + 1), value);
            if (name.equals("filename") && plain == null) {
                plain = value.toString();
            } else if (name.equals("filename*") && extended == null) {
                extended = decodeExtended(value.toString());
            }
        }
        String name = extended != null ? extended : plain;
        return name == null || name.isEmpty() ? null : name;
    }

    private static int skipSpaces(String header, int from) {
        int pos = from;
        while (pos < header.length() && header.charAt(pos) == ' ') {
            pos++;
        }
        return pos;
    }

    // Appends the parameter value that starts at from to value and returns the
    // index of the next ';', or -1.
    private static int readValue(String header, int from, StringBuilder value) {
        if (from < header.length() && header.charAt(from) == '"') {
            return readQuoted(header, from + 1, value);
        }
        int end = header.indexOf(';', from);
        value.append((end < 0 ? header.substring(from) : header.substring(from, end)).trim());
        return end;
    }

    private static int readQuoted(String header, int from, StringBuilder value) {
        int pos = from;
        while (pos < header.length() && header.charAt(pos) != '"') {
            char c = header.charAt(pos);
            if (c == '\\' && pos + 1 < header.length()) {
                c = header.charAt(++pos);
            }
            value.append(c);
            pos++;
        }
        return header.indexOf(';', pos);
    }

    private static String decodeExtended(String value) {
        int first = value.indexOf('\'');
        int second = first < 0 ? -1 : value.indexOf('\'', first + 1);
        if (second < 0) {
            return null;
        }
        Charset charset = charset(value.substring(0, first));
        if (charset == null) {
            return null;
        }
        return percentDecode(value.substring(second + 1), charset);
    }

    private static Charset charset(String name) {
        if (name.equalsIgnoreCase("UTF-8")) {
            return StandardCharsets.UTF_8;
        }
        if (name.equalsIgnoreCase("ISO-8859-1")) {
            return StandardCharsets.ISO_8859_1;
        }
        return null;
    }

    private static String percentDecode(String encoded, Charset charset) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int p = 0;
        while (p < encoded.length()) {
            char c = encoded.charAt(p);
            if (c == '%') {
                int octet = hexOctet(encoded, p + 1);
                if (octet < 0) {
                    return null;
                }
                bytes.write(octet);
                p += 3;
            } else if (c > 0x20 && c < 0x7f) {
                bytes.write(c);
                p++;
            } else {
                return null;
            }
        }
        return new String(bytes.toByteArray(), charset);
    }

    // The octet written as two hex digits at index, or -1.
    private static int hexOctet(String encoded, int index) {
        if (index + 1 >= encoded.length()) {
            return -1;
        }
        int hi = Character.digit(encoded.charAt(index), 16);
        int lo = Character.digit(encoded.charAt(index + 1), 16);
        if (hi < 0 || lo < 0) {
            return -1;
        }
        return (hi << 4) | lo;
    }
}
