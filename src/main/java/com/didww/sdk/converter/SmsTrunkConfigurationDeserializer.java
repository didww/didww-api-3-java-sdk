package com.didww.sdk.converter;

import com.didww.sdk.resource.smstrunkconfiguration.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads an SMS trunk configuration {@code {"type": ..., "attributes": {...}}}. An unknown type is kept as a
 * {@link GenericSmsTrunkConfiguration} with its attribute tree.
 */
public class SmsTrunkConfigurationDeserializer extends JsonDeserializer<SmsTrunkConfiguration> {

    private static final Map<String, Class<? extends SmsTrunkConfiguration>> TYPE_MAP = new LinkedHashMap<>();
    static {
        TYPE_MAP.put("sms_to_email", SmsToEmailConfiguration.class);
        TYPE_MAP.put("http_in", HttpInConfiguration.class);
        TYPE_MAP.put("http_out", HttpOutConfiguration.class);
        TYPE_MAP.put("smpp_esme", SmppEsmeConfiguration.class);
        TYPE_MAP.put("smpp_smsc", SmppSmscConfiguration.class);
    }

    @Override
    public SmsTrunkConfiguration deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }

        JsonNode typeNode = node.get("type");
        if (typeNode == null || !typeNode.isTextual()) {
            throw new IOException("Missing 'type' field in SMS trunk configuration");
        }
        String type = typeNode.asText();

        JsonNode attributes = node.get("attributes");
        if (attributes != null && attributes.isNull()) {
            attributes = null;
        }
        if (attributes != null && !attributes.isObject()) {
            throw new IOException("'attributes' of SMS trunk configuration is not an object");
        }

        Class<? extends SmsTrunkConfiguration> clazz = TYPE_MAP.get(type);
        if (clazz != null) {
            if (attributes == null) {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new IOException("Cannot instantiate " + clazz.getName(), e);
                }
            }
            return mapper.treeToValue(attributes, clazz);
        }

        return new GenericSmsTrunkConfiguration(type,
                attributes == null ? mapper.createObjectNode() : ((ObjectNode) attributes).deepCopy());
    }
}
