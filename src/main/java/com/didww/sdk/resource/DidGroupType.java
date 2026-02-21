package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("did_group_types")
public class DidGroupType extends BaseResource {

    public static DidGroupType build(String id) {
        return BaseResource.build(DidGroupType.class, id);
    }

    @JsonProperty("name")
    private String name;

    public String getName() {
        return name;
    }
}
