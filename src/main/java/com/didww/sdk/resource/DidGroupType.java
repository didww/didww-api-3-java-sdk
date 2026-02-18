package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("did_group_types")
public class DidGroupType extends BaseResource {

    @JsonProperty("name")
    private String name;

    public String getName() {
        return name;
    }
}
