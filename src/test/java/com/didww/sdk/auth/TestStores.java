package com.didww.sdk.auth;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/** Token stores for tests. They are not {@link MemoryTokenStore}, so their calls are bounded. */
final class TestStores {

    private TestStores() {
    }

    /** A plain map store that records the keys and TTLs it was given. */
    static class Recording implements TokenStore {
        final ConcurrentHashMap<String, AccessToken> tokens = new ConcurrentHashMap<>();
        final List<String> keys = new ArrayList<>();
        final List<Duration> ttls = new ArrayList<>();
        int deletes;

        @Override
        public AccessToken get(String key) {
            return tokens.get(key);
        }

        @Override
        public synchronized void set(String key, AccessToken token, Duration ttl) {
            keys.add(key);
            ttls.add(ttl);
            tokens.put(key, token);
        }

        @Override
        public synchronized void delete(String key) {
            deletes++;
            tokens.remove(key);
        }
    }

    /** A store whose delete does nothing, so that a refused token stays in it. */
    static final class KeepingRefused extends Recording {
        @Override
        public synchronized void delete(String key) {
            deletes++;
        }
    }

    /** A store whose calls throw the given exception. */
    static final class Failing implements TokenStore {
        private final boolean get;
        private final boolean set;
        private final boolean delete;
        final ConcurrentHashMap<String, AccessToken> tokens = new ConcurrentHashMap<>();

        Failing(boolean get, boolean set, boolean delete) {
            this.get = get;
            this.set = set;
            this.delete = delete;
        }

        @Override
        public AccessToken get(String key) {
            if (get) {
                throw new IllegalStateException("store get failed");
            }
            return tokens.get(key);
        }

        @Override
        public void set(String key, AccessToken token, Duration ttl) {
            if (set) {
                throw new IllegalStateException("store set failed");
            }
            tokens.put(key, token);
        }

        @Override
        public void delete(String key) {
            if (delete) {
                throw new IllegalStateException("store delete failed");
            }
            tokens.remove(key);
        }
    }

    /** A store whose get blocks until released (or interrupted). */
    static final class Stalling extends Recording {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        @Override
        public AccessToken get(String key) {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return super.get(key);
        }
    }

    /** A store that runs a hook before each get. */
    static final class Hooked extends Recording {
        private final Consumer<Hooked> beforeGet;
        int gets;

        Hooked(Consumer<Hooked> beforeGet) {
            this.beforeGet = beforeGet;
        }

        @Override
        public AccessToken get(String key) {
            synchronized (this) {
                gets++;
            }
            beforeGet.accept(this);
            return super.get(key);
        }
    }
}
