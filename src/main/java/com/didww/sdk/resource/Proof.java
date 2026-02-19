package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Type("proofs")
public class Proof extends BaseResource {

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("expires_at")
    private OffsetDateTime expiresAt;

    @Relationship("proof_type")
    private ProofType proofType;

    @Relationship("files")
    private List<EncryptedFile> files;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public ProofType getProofType() {
        return proofType;
    }

    public void setProofType(ProofType proofType) {
        this.proofType = proofType;
    }

    public List<EncryptedFile> getFiles() {
        return files;
    }

    public void setFiles(List<EncryptedFile> files) {
        this.files = files;
    }
}
