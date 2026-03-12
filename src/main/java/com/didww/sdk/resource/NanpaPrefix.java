package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("nanpa_prefixes")
public class NanpaPrefix extends BaseResource {

    @JsonProperty("npa")
    private String npa;

    @JsonProperty("nxx")
    private String nxx;

    @Relationship("country")
    private Country country;

    @Relationship("region")
    private Region region;

    public String getNpa() {
        return npa;
    }

    public String getNxx() {
        return nxx;
    }

    public Country getCountry() {
        return country;
    }

    public Region getRegion() {
        return region;
    }
}
