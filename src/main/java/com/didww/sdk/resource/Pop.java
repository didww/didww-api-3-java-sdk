package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("pops")
public class Pop extends BaseResource {

    public static Pop build(String id) {
        return BaseResource.build(Pop.class, id);
    }

    @JsonProperty("name")
    private String name;

    public String getName() {
        return name;
    }
}
