package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("pops")
public class Pop extends BaseResource {

    @JsonProperty("name")
    private String name;

    public String getName() {
        return name;
    }
}
