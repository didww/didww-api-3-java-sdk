package com.didww.sdk.converter;

import com.didww.sdk.resource.configuration.TrunkConfiguration;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class TrunkConfigurationSerializer extends JsonSerializer<TrunkConfiguration> {

    @Override
    public void serialize(TrunkConfiguration value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        ObjectMapper mapper = (ObjectMapper) gen.getCodec();
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.put("type", value.getType());
        wrapper.set("attributes", mapper.valueToTree(value));
        gen.writeTree(wrapper);
    }
}
