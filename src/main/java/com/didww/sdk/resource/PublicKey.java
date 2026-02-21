package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("public_keys")
public class PublicKey extends BaseResource {

    public static PublicKey build(String id) {
        return BaseResource.build(PublicKey.class, id);
    }

    @JsonProperty("key")
    private String key;

    public String getKey() {
        return key;
    }
}
