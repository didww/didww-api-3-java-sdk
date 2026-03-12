package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("public_keys")

@Getter
public class PublicKey extends BaseResource {

    @JsonProperty("key")
    private String key;
}
