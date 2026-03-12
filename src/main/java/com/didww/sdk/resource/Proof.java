package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Type("proofs")

@Getter
public class Proof extends BaseResource {

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("expires_at")
    private OffsetDateTime expiresAt;

    @Relationship("entity")
    private ProofEntity entity;

    @Relationship("proof_type")
    private ProofType proofType;

    @Relationship("files")
    private List<EncryptedFile> files;

    public void setEntity(ProofEntity entity) {
        this.entity = markDirty("entity", entity);
    }

    public void setProofType(ProofType proofType) {
        this.proofType = markDirty("proofType", proofType);
    }

    public void setFiles(List<EncryptedFile> files) {
        this.files = markDirty("files", files);
    }
}
