package com.didww.sdk.auth;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * A {@link TokenStore} that keeps tokens in process memory; expired entries are
 * dropped when read or when another token is stored. It is safe for concurrent
 * use. A process-wide instance is the default store of every
 * {@code DidwwClient} built without its own.
 */
public final class MemoryTokenStore implements TokenStore {
    private static final long NO_EXPIRY = Long.MIN_VALUE;

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final LongSupplier nanoTime;

    /** Creates an empty store. */
    public MemoryTokenStore() {
        this(System::nanoTime);
    }

    MemoryTokenStore(LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    @Override
    public AccessToken get(String key) {
        Entry entry = entries.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired(nanoTime.getAsLong())) {
            entries.remove(key, entry);
            return null;
        }
        return entry.token;
    }

    @Override
    public void set(String key, AccessToken token, Duration ttl) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(token, "token must not be null");
        long now = nanoTime.getAsLong();
        long expiresAt = ttl != null && !ttl.isNegative() && !ttl.isZero() ? now + ttl.toNanos() : NO_EXPIRY;
        entries.values().removeIf(e -> e.isExpired(now));
        entries.put(key, new Entry(token, expiresAt));
    }

    @Override
    public void delete(String key) {
        entries.remove(key);
    }

    /**
     * Describes the store without its tokens.
     *
     * @return the number of entries
     */
    @Override
    public String toString() {
        return "MemoryTokenStore{entries=" + entries.size() + "}";
    }

    private static final class Entry {
        private final AccessToken token;
        private final long expiresAt;

        private Entry(AccessToken token, long expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired(long now) {
            return expiresAt != NO_EXPIRY && now - expiresAt >= 0;
        }
    }
}
