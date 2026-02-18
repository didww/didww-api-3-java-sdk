package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("supporting_document_templates")
public class SupportingDocumentTemplate {

    @Id
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("permanent")
    private Boolean permanent;

    @JsonProperty("url")
    private String url;

    public String getId() {
        return id;
    }

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
