package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("pops")

@Getter
public class Pop extends BaseResource {

    @JsonProperty("name")
    private String name;
}
