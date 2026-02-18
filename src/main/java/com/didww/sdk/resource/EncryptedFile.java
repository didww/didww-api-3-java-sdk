package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;

@Type("encrypted_files")
public class EncryptedFile implements HasId {

    @Id
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("expire_at")
    private OffsetDateTime expireAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(OffsetDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
