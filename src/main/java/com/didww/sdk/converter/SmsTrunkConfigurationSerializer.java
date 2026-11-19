package com.didww.sdk.converter;

import com.didww.sdk.resource.smstrunkconfiguration.GenericSmsTrunkConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.HttpOutConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmppEsmeConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmsTrunkConfiguration;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Arrays;

/**
 * Writes an SMS trunk configuration as {@code {"type": ..., "attributes": {...}}}. A
 * {@link GenericSmsTrunkConfiguration} is written verbatim, explicit nulls included; the read-only credentials
 * of {@code http_out} and {@code smpp_esme} configurations (subclasses included) are never written.
 */
public class SmsTrunkConfigurationSerializer extends JsonSerializer<SmsTrunkConfiguration> {

    @Override
    public void serialize(SmsTrunkConfiguration value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        ObjectMapper mapper = (ObjectMapper) gen.getCodec();
        ObjectNode attributes;
        if (value instanceof GenericSmsTrunkConfiguration) {
            attributes = ((GenericSmsTrunkConfiguration) value).getAttributes().deepCopy();
        } else {
            attributes = mapper.valueToTree(value);
            if (value instanceof HttpOutConfiguration) {
                attributes.remove(Arrays.asList("auth_name", "auth_password"));
            }
            if (value instanceof SmppEsmeConfiguration) {
                attributes.remove(Arrays.asList("system_id", "password"));
            }
        }
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.put("type", value.getType());
        wrapper.set("attributes", attributes);
        gen.writeTree(wrapper);
    }
}
