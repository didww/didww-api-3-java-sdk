package com.didww.sdk.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;

class PolymorphicJsonHelper {

    static <T> T deserialize(ObjectMapper mapper, JsonNode node,
                             Map<String, Class<? extends T>> typeMap,
                             String entityName) throws IOException {
        String type = node.has("type") ? node.get("type").asText() : null;
        if (type == null) {
            throw new IOException("Missing 'type' field in " + entityName);
        }

        Class<? extends T> clazz = typeMap.get(type);
        if (clazz == null) {
            throw new IOException("Unknown " + entityName + " type: " + type);
        }

        JsonNode attributes = node.get("attributes");
        if (attributes == null) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IOException("Cannot instantiate " + clazz.getName(), e);
            }
        }

        return mapper.treeToValue(attributes, clazz);
    }

    static void serialize(Object value, String type, JsonGenerator gen,
                          ObjectMapper mapper) throws IOException {
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.put("type", type);
        wrapper.set("attributes", mapper.valueToTree(value));
        gen.writeTree(wrapper);
    }
}
