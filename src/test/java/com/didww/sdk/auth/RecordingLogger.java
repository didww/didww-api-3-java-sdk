package com.didww.sdk.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/** A {@link System.Logger} that records what it is given. */
public class RecordingLogger implements System.Logger {

    static final class Entry {
        final Level level;
        final String message;
        final Throwable thrown;

        Entry(Level level, String message, Throwable thrown) {
            this.level = level;
            this.message = message;
            this.thrown = thrown;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private final boolean debug;

    RecordingLogger(boolean debug) {
        this.debug = debug;
    }

    public RecordingLogger() {
        this(false);
    }

    synchronized List<Entry> entries() {
        return new ArrayList<>(entries);
    }

    public synchronized List<String> messages(Level level) {
        return entries.stream().filter(e -> e.level == level).map(e -> e.message).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public boolean isLoggable(Level level) {
        return debug || level.getSeverity() >= Level.INFO.getSeverity();
    }

    @Override
    public synchronized void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
        entries.add(new Entry(level, msg, thrown));
    }

    @Override
    public synchronized void log(Level level, ResourceBundle bundle, String format, Object... params) {
        entries.add(new Entry(level, format, null));
    }
}
