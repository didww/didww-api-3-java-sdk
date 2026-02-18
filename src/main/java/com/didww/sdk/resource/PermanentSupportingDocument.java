package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Type("permanent_supporting_documents")
public class PermanentSupportingDocument extends BaseResource {

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @Relationship("identity")
    private Identity identity;

    @Relationship("template")
    private SupportingDocumentTemplate template;

    @Relationship("files")
    private List<EncryptedFile> files;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public SupportingDocumentTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SupportingDocumentTemplate template) {
        this.template = template;
    }

    public List<EncryptedFile> getFiles() {
        return files;
    }

    public void setFiles(List<EncryptedFile> files) {
        this.files = files;
    }
}
