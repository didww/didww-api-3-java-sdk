package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("cities")

@Getter
public class City extends BaseResource {

    @JsonProperty("name")
    private String name;

    @Relationship("country")
    private Country country;

    @Relationship("region")
    private Region region;

    @Relationship("area")
    private Area area;
}
