package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.util.List;

@Type("countries")

@Getter
public class Country extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("iso")
    private String iso;

    @Relationship("regions")
    private List<Region> regions;
}
