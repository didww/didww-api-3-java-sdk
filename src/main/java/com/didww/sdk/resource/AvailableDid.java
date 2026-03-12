package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("available_dids")

@Getter
public class AvailableDid extends BaseResource {

    @JsonProperty("number")
    private String number;

    @Relationship("did_group")
    private DidGroup didGroup;

    @Relationship("nanpa_prefix")
    private NanpaPrefix nanpaPrefix;
}
