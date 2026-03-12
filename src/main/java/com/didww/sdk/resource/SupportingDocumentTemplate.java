package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("supporting_document_templates")
public class SupportingDocumentTemplate extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("permanent")
    private Boolean permanent;

    @JsonProperty("url")
    private String url;

    public String getName() {
        return name;
    }

    public Boolean getPermanent() {
        return permanent;
    }

    public String getUrl() {
        return url;
    }
}
