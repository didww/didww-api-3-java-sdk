package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("cities")
public class City extends BaseResource {

    @JsonProperty("name")
    private String name;

    @Relationship("country")
    private Country country;

    @Relationship("region")
    private Region region;

    public String getName() {
        return name;
    }

    public Country getCountry() {
        return country;
    }

    public Region getRegion() {
        return region;
    }
}
