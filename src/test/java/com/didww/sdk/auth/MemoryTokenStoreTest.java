package com.didww.sdk.auth;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryTokenStoreTest {
    private final AtomicLong now = new AtomicLong(-5_000_000_000L);
    private final MemoryTokenStore store = new MemoryTokenStore(now::get);

    private static AccessToken token(String value) {
        return new AccessToken(value, "Bearer", 900, Collections.singletonList("api.didww.com"), "full",
                "2026-10-22", Instant.parse("2026-10-22T08:00:00Z"));
    }

    @Test
    void testGetMissing() {
        assertThat(store.get("missing")).isNull();
    }

    @Test
    void testExpiry() {
        store.set("k", token("t"), Duration.ofSeconds(10));

        now.addAndGet(Duration.ofSeconds(9).toNanos());
        assertThat(store.get("k").getAccessToken()).isEqualTo("t");

        now.addAndGet(Duration.ofSeconds(1).toNanos());
        assertThat(store.get("k")).isNull();
        assertThat(store.toString()).isEqualTo("MemoryTokenStore{entries=0}");
    }

    @Test
    void testWithoutTtl() {
        store.set("zero", token("t"), Duration.ZERO);
        store.set("negative", token("t"), Duration.ofSeconds(-1));
        store.set("null", token("t"), null);

        now.addAndGet(Duration.ofDays(365).toNanos());
        assertThat(store.get("zero")).isNotNull();
        assertThat(store.get("negative")).isNotNull();
        assertThat(store.get("null")).isNotNull();
    }

    @Test
    void testReplace() {
        store.set("k", token("t1"), Duration.ofSeconds(10));
        store.set("k", token("t2"), Duration.ofSeconds(10));

        assertThat(store.get("k").getAccessToken()).isEqualTo("t2");
    }

    @Test
    void testDelete() {
        store.set("k", token("t"), Duration.ofSeconds(10));
        store.delete("k");
        store.delete("other");

        assertThat(store.get("k")).isNull();
    }

    @Test
    void testExpiredEntriesDroppedOnSet() {
        store.set("old", token("t"), Duration.ofSeconds(1));
        now.addAndGet(Duration.ofSeconds(2).toNanos());

        store.set("new", token("t"), Duration.ofSeconds(1));

        assertThat(store.toString()).isEqualTo("MemoryTokenStore{entries=1}");
    }

    @Test
    void testToStringHidesTokens() {
        store.set("k", token("secret-token"), Duration.ofSeconds(10));

        assertThat(store.toString()).isEqualTo("MemoryTokenStore{entries=1}").doesNotContain("secret-token");
    }
}
