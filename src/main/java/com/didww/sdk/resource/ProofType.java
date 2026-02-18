package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("proof_types")
public class ProofType {

    @Id
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("entity_type")
    private String entityType;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEntityType() {
        return entityType;
    }
}
