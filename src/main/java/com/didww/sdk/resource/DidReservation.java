package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;

@Type("did_reservations")
public class DidReservation extends BaseResource {

    @JsonProperty("description")
    private String description;

    @JsonProperty(value = "expire_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime expireAt;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @Relationship("available_did")
    private AvailableDid availableDid;

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

    public AvailableDid getAvailableDid() {
        return availableDid;
    }

    public void setAvailableDid(AvailableDid availableDid) {
        this.availableDid = availableDid;
    }
}
