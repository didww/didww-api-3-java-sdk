package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("areas")
public class Area extends BaseResource {

    public static Area build(String id) {
        return BaseResource.build(Area.class, id);
    }

    @JsonProperty("name")
    private String name;

    @Relationship("country")
    private Country country;

    public String getName() {
        return name;
    }

    public Country getCountry() {
        return country;
    }
}
