package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("available_dids")
public class AvailableDid {

    @Id
    private String id;

    @JsonProperty("number")
    private String number;

    @Relationship("did_group")
    private DidGroup didGroup;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public DidGroup getDidGroup() {
        return didGroup;
    }
}
