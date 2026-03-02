package com.didww.sdk.converter;

public final class DirtySerializationContext {

    private static final ThreadLocal<Boolean> DIRTY_ONLY = ThreadLocal.withInitial(() -> false);

    private DirtySerializationContext() {
    }

    public static void enableDirtyOnlyMode() {
        DIRTY_ONLY.set(true);
    }

    public static void disableDirtyOnlyMode() {
        DIRTY_ONLY.remove();
    }

    public static boolean isDirtyOnlyModeEnabled() {
        return DIRTY_ONLY.get();
    }
}
