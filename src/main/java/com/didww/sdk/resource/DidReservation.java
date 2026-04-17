package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;

@Type("did_reservations")

@Getter
public class DidReservation extends BaseResource {

    @JsonProperty("description")
    private String description;

    @JsonProperty(value = "expires_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime expiresAt;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @Relationship("available_did")
    private AvailableDid availableDid;

    public void setDescription(String description) {
        this.description = markDirty("description", description);
    }

    public void setAvailableDid(AvailableDid availableDid) {
        this.availableDid = markDirty("availableDid", availableDid);
    }
}
