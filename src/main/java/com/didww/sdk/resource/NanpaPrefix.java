package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("nanpa_prefixes")

@Getter
public class NanpaPrefix extends BaseResource {

    @JsonProperty("npa")
    private String npa;

    @JsonProperty("nxx")
    private String nxx;

    @Relationship("country")
    private Country country;

    @Relationship("region")
    private Region region;
}
