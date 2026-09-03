package com.didww.sdk.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetaMapTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void readsNonStringValuesAsStrings() throws Exception {
        MetaMap meta = objectMapper.readValue(
                "{\"monthly_price\": \"2.5\", \"setup_price\": 0, \"unpriced\": null, \"enabled\": true}",
                MetaMap.class);

        assertThat(meta.get("monthly_price")).isEqualTo("2.5");
        assertThat(meta.get("setup_price")).isEqualTo("0");
        assertThat(meta.get("unpriced")).isNull();
        assertThat(meta.get("enabled")).isEqualTo("true");
    }
}
