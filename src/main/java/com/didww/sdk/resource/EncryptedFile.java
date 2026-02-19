package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;

@Type("encrypted_files")
public class EncryptedFile extends BaseResource {

    @JsonProperty("description")
    private String description;

    @JsonProperty(value = "expire_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime expireAt;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getExpireAt() {
        return expireAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
