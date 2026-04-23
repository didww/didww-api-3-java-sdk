package com.didww.sdk.converter;

import com.didww.sdk.resource.authenticationmethod.AuthenticationMethod;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AuthenticationMethodSerializer extends JsonSerializer<AuthenticationMethod> {

    @Override
    public void serialize(AuthenticationMethod value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        PolymorphicJsonHelper.serialize(value, value.getType(), gen,
                (ObjectMapper) gen.getCodec());
    }
}
