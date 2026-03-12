package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("supporting_document_templates")

@Getter
public class SupportingDocumentTemplate extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("permanent")
    private Boolean permanent;

    @JsonProperty("url")
    private String url;
}
