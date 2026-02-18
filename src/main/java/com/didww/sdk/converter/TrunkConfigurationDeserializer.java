package com.didww.sdk.converter;

import com.didww.sdk.resource.configuration.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TrunkConfigurationDeserializer extends JsonDeserializer<TrunkConfiguration> {

    private static final Map<String, Class<? extends TrunkConfiguration>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("sip_configurations", SipConfiguration.class);
        TYPE_MAP.put("h323_configurations", H323Configuration.class);
        TYPE_MAP.put("iax2_configurations", Iax2Configuration.class);
        TYPE_MAP.put("pstn_configurations", PstnConfiguration.class);
    }

    @Override
    public TrunkConfiguration deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }

        String type = node.has("type") ? node.get("type").asText() : null;
        if (type == null) {
            throw new IOException("Missing 'type' field in trunk configuration");
        }

        Class<? extends TrunkConfiguration> clazz = TYPE_MAP.get(type);
        if (clazz == null) {
            throw new IOException("Unknown trunk configuration type: " + type);
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
}
