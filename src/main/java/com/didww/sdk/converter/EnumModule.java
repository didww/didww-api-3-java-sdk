package com.didww.sdk.converter;

import com.didww.sdk.resource.enums.IntEnum;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class EnumModule extends SimpleModule {

    public EnumModule() {
        super("EnumModule");
        setDeserializers(new SimpleDeserializers() {
            @Override
            @SuppressWarnings("unchecked")
            public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                    DeserializationConfig config, BeanDescription beanDesc) {
                if (IntEnum.class.isAssignableFrom(type)) {
                    return new IntEnumDeserializer((Class<? extends Enum<?>>) type);
                }
                return null;
            }
        });
    }

    private static class IntEnumDeserializer extends JsonDeserializer<Enum<?>> {
        private final Class<? extends Enum<?>> enumType;

        IntEnumDeserializer(Class<? extends Enum<?>> enumType) {
            this.enumType = enumType;
        }

        @Override
        public Enum<?> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            int value = p.getIntValue();
            for (Enum<?> constant : enumType.getEnumConstants()) {
                if (((IntEnum) constant).getValue() == value) {
                    return constant;
                }
            }
            if (ctx.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
                return null;
            }
            throw new IllegalArgumentException("Unknown " + enumType.getSimpleName() + ": " + value);
        }
    }
}
