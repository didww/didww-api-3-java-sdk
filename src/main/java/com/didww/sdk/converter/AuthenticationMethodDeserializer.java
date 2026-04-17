package com.didww.sdk.converter;

import com.didww.sdk.resource.authenticationmethod.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthenticationMethodDeserializer extends JsonDeserializer<AuthenticationMethod> {

    private static final Map<String, Class<? extends AuthenticationMethod>> TYPE_MAP = new LinkedHashMap<>();
    static {
        TYPE_MAP.put("ip_only", IpOnlyAuthenticationMethod.class);
        TYPE_MAP.put("credentials_and_ip", CredentialsAndIpAuthenticationMethod.class);
        TYPE_MAP.put("twilio", TwilioAuthenticationMethod.class);
    }

    @Override
    public AuthenticationMethod deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }

        String type = node.has("type") ? node.get("type").asText() : null;
        if (type == null) {
            throw new IOException("Missing 'type' field in authentication_method");
        }

        Class<? extends AuthenticationMethod> clazz = TYPE_MAP.get(type);
        JsonNode attributes = node.get("attributes");

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

        // Unknown type: wrap in GenericAuthenticationMethod for forward-compatibility
        GenericAuthenticationMethod generic = new GenericAuthenticationMethod(type);
        if (attributes != null) {
            attributes.fields().forEachRemaining(entry ->
                    generic.setAttribute(entry.getKey(), mapper.convertValue(entry.getValue(), Object.class)));
        }
        return generic;
    }
}
