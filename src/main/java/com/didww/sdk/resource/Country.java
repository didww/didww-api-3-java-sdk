package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.util.List;

@Type("countries")
public class Country extends BaseResource {

    public static Country build(String id) {
        return BaseResource.build(Country.class, id);
    }

    @JsonProperty("name")
    private String name;

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("iso")
    private String iso;

    @Relationship("regions")
    private List<Region> regions;

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getIso() {
        return iso;
    }

    public List<Region> getRegions() {
        return regions;
    }
}
