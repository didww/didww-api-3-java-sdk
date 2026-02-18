package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("available_dids")
public class AvailableDid extends BaseResource {

    @JsonProperty("number")
    private String number;

    @Relationship("did_group")
    private DidGroup didGroup;

    public String getNumber() {
        return number;
    }

    public DidGroup getDidGroup() {
        return didGroup;
    }
}
