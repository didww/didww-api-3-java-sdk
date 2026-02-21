package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("cities")
public class City extends BaseResource {

    public static City build(String id) {
        return BaseResource.build(City.class, id);
    }

    @JsonProperty("name")
    private String name;

    @Relationship("country")
    private Country country;

    @Relationship("region")
    private Region region;

    @Relationship("area")
    private Area area;

    public String getName() {
        return name;
    }

    public Country getCountry() {
        return country;
    }

    public Region getRegion() {
        return region;
    }

    public Area getArea() {
        return area;
    }
}
