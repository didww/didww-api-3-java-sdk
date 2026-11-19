package com.didww.sdk.prices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HttpDatesTest {

    @Test
    void testFormatPadsTheDayAndDropsFractions() {
        assertThat(HttpDates.format(Instant.parse("2026-10-01T08:05:03.999Z"))).isEqualTo("Thu, 01 Oct 2026 08:05:03 GMT");
        assertThat(HttpDates.format(Instant.parse("2026-12-31T23:59:59Z"))).isEqualTo("Thu, 31 Dec 2026 23:59:59 GMT");
    }

    @Test
    void testParseImfFixdate() {
        assertThat(HttpDates.parse("Thu, 01 Oct 2026 08:05:03 GMT")).isEqualTo(Instant.parse("2026-10-01T08:05:03Z"));
        assertThat(HttpDates.parse(" Thu, 01 Oct 2026 08:05:03 GMT ")).isEqualTo(Instant.parse("2026-10-01T08:05:03Z"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Fri, 01 Oct 2026 08:05:03 GMT",
            "Thu, 1 Oct 2026 08:05:03 GMT",
            "01 Oct 2026 08:05:03 GMT",
            "Thu, 01 Oct 2026 08:05:03 +0000",
            "Thu, 01 Oct 2026 08:05:03 UTC",
            "Thu, 01 oct 2026 08:05:03 GMT",
            "Thursday, 01-Oct-26 08:05:03 GMT",
            "Thu Oct  1 08:05:03 2026",
            "Tue, 31 Feb 2026 08:05:03 GMT",
            "Thu, 01 Oct 2026 24:00:00 GMT",
            ""
    })
    void testParseRejectsOtherForms(String value) {
        assertThat(HttpDates.parse(value)).isNull();
    }

    @Test
    void testParseNull() {
        assertThat(HttpDates.parse(null)).isNull();
    }
}
