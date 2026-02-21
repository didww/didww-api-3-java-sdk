package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("proof_types")
public class ProofType extends BaseResource {

    public static ProofType build(String id) {
        return BaseResource.build(ProofType.class, id);
    }

    @JsonProperty("name")
    private String name;

    @JsonProperty("entity_type")
    private String entityType;

    public String getName() {
        return name;
    }

    public String getEntityType() {
        return entityType;
    }
}
