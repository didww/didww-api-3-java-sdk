package com.didww.sdk.auth;

import java.time.Duration;

/**
 * Caches access tokens.
 *
 * <p>Keys are opaque strings that already cover the token service URL, the API
 * key (hashed), the audience, the API version and the scope, so one store may
 * be shared by several clients or processes. A store holds live bearer tokens:
 * protect it like the API key. A token is stored for the lifetime granted by
 * the token service; it is replaced when the API refuses it (401), not ahead
 * of its expiry.
 *
 * <p>Implementations must be safe for concurrent use. The SDK logs and
 * tolerates a {@link RuntimeException} thrown by a store, and gives up waiting
 * for a call to a store other than {@link MemoryTokenStore} after 10 seconds;
 * an abandoned call keeps its thread until it returns, so bound the store's own
 * I/O as well, for example with connect and read timeouts on a Redis client.
 */
public interface TokenStore {

    /**
     * Returns the token stored under {@code key}.
     *
     * @param key the cache key
     * @return the token, or {@code null} when there is none or it has expired
     */
    AccessToken get(String key);

    /**
     * Stores {@code token} under {@code key} for {@code ttl}.
     *
     * @param key   the cache key
     * @param token the token
     * @param ttl   how long to keep it; zero or negative means no expiry
     */
    void set(String key, AccessToken token, Duration ttl);

    /**
     * Removes the token stored under {@code key}, if any.
     *
     * @param key the cache key
     */
    void delete(String key);
}
