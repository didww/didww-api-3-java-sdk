package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("public_keys")
public class PublicKey {

    @Id
    private String id;

    @JsonProperty("key")
    private String key;

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }
}
