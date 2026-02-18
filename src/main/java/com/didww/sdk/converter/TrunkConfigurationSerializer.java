package com.didww.sdk.converter;

import com.didww.sdk.resource.configuration.TrunkConfiguration;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class TrunkConfigurationSerializer extends JsonSerializer<TrunkConfiguration> {

    @Override
    public void serialize(TrunkConfiguration value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        PolymorphicJsonHelper.serialize(value, value.getType(), gen,
                (ObjectMapper) gen.getCodec());
    }
}
