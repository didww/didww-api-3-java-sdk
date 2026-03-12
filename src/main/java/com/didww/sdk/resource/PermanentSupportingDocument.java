package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Type("permanent_supporting_documents")

@Getter
public class PermanentSupportingDocument extends BaseResource {

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @Relationship("identity")
    private Identity identity;

    @Relationship("template")
    private SupportingDocumentTemplate template;

    @Relationship("files")
    private List<EncryptedFile> files;

    public void setIdentity(Identity identity) {
        this.identity = markDirty("identity", identity);
    }

    public void setTemplate(SupportingDocumentTemplate template) {
        this.template = markDirty("template", template);
    }

    public void setFiles(List<EncryptedFile> files) {
        this.files = markDirty("files", files);
    }
}
