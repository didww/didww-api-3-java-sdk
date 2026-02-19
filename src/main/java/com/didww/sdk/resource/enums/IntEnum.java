package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface IntEnum {
    Map<IntEnum, Integer> REGISTRY = new ConcurrentHashMap<>();

    static void register(IntEnum instance, int value) {
        REGISTRY.put(instance, value);
    }

    @JsonValue
    default int getValue() {
        return REGISTRY.get(this);
    }
}
