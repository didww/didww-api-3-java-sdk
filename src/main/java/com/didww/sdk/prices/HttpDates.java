package com.didww.sdk.prices;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

// HTTP dates in the IMF-fixdate form only, such as "Thu, 01 Oct 2026 08:05:03 GMT".
// Parsing is strict: a day without its leading zero, another zone, a weekday
// that does not match the date or an impossible date are refused.
final class HttpDates {
    private static final DateTimeFormatter IMF_FIXDATE = DateTimeFormatter
            .ofPattern("EEE, dd MMM uuuu HH:mm:ss 'GMT'", Locale.ROOT)
            .withZone(ZoneOffset.UTC)
            .withResolverStyle(ResolverStyle.STRICT);

    private HttpDates() {
    }

    static String format(Instant instant) {
        return IMF_FIXDATE.format(instant.truncatedTo(ChronoUnit.SECONDS));
    }

    static Instant parse(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ZonedDateTime.parse(value.trim(), IMF_FIXDATE).toInstant();
        } catch (DateTimeException e) {
            return null;
        }
    }
}
