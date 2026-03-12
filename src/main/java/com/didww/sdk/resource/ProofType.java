package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("proof_types")

@Getter
public class ProofType extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("entity_type")
    private String entityType;
}
