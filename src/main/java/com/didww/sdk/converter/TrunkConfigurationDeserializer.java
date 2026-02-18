package com.didww.sdk.converter;

import com.didww.sdk.resource.configuration.TrunkConfiguration;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TrunkConfigurationDeserializer extends JsonDeserializer<TrunkConfiguration> {

    @Override
    public TrunkConfiguration deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }

        return PolymorphicJsonHelper.deserialize(mapper, node,
                TrunkConfiguration.getTypeMap(), "trunk configuration");
    }
}
