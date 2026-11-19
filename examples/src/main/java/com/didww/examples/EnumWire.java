package com.didww.examples;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Converts an SDK enum constant to the string the API uses for it, for filters such as
 * {@code filter("number_type", EnumWire.value(NumberType.TOLL_FREE))} ({@code "toll_free"}).
 * The wire value is the {@code @JsonProperty} of the constant; {@code name()} is not the wire form.
 */
final class EnumWire {

    private EnumWire() {
    }

    static String value(Enum<?> constant) {
        try {
            JsonProperty property = constant.getDeclaringClass()
                    .getField(constant.name())
                    .getAnnotation(JsonProperty.class);
            return property != null ? property.value() : constant.name();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }
}
