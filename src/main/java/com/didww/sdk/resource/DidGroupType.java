package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("did_group_types")

@Getter
public class DidGroupType extends BaseResource {

    @JsonProperty("name")
    private String name;
}
