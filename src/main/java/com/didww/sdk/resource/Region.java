package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("regions")
public class Region extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("iso")
    private String iso;

    @Relationship("country")
    private Country country;

    public String getName() {
        return name;
    }

    public String getIso() {
        return iso;
    }

    public Country getCountry() {
        return country;
    }
}
