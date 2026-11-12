package com.didww.sdk.resource.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A value the API sends as a string and may extend with new strings, such as a codec. A value this
 * version of the SDK does not know is kept as read and sent back unchanged. The known values are
 * constants of the subclass, and reading a known value returns that constant.
 */
public abstract class StringValue {
    private final String value;

    StringValue(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    /**
     * @return the name of the value in the API
     */
    @JsonValue
    public final String getValue() {
        return value;
    }

    @Override
    public final boolean equals(Object other) {
        return other != null && other.getClass() == getClass() && value.equals(((StringValue) other).value);
    }

    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    @Override
    public final String toString() {
        return value;
    }

    @SafeVarargs
    static <T extends StringValue> Map<String, T> byValue(T... constants) {
        Map<String, T> map = new LinkedHashMap<>();
        for (T constant : constants) {
            map.put(constant.getValue(), constant);
        }
        return Collections.unmodifiableMap(map);
    }
}
