package com.didww.sdk.resource.authenticationmethod;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Forward-compatible wrapper for unknown authentication_method types.
 * Preserves all attributes as a generic map so unknown future types are
 * not lost during round-trip serialization.
 */
public class GenericAuthenticationMethod extends AuthenticationMethod {

    private final String type;
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public GenericAuthenticationMethod(String type) {
        this.type = type;
    }

    @Override
    @JsonIgnore
    public String getType() {
        return type;
    }

    @JsonAnyGetter
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @JsonAnySetter
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}
