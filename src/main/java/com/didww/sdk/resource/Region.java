package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("regions")

@Getter
public class Region extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("iso")
    private String iso;

    @Relationship("country")
    private Country country;
}
