package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Type("addresses")

@Getter
public class Address extends BaseResource implements ProofEntity {

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("address")
    private String address;

    @JsonProperty("description")
    private String description;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty(value = "verified", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean verified;

    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    @Relationship("country")
    private Country country;

    @Relationship("identity")
    private Identity identity;

    @Relationship("proofs")
    private List<Proof> proofs;

    @Relationship("area")
    private Area area;

    @Relationship("city")
    private City city;

    public void setCityName(String cityName) {
        this.cityName = markDirty("cityName", cityName);
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = markDirty("postalCode", postalCode);
    }

    public void setAddress(String address) {
        this.address = markDirty("address", address);
    }

    public void setDescription(String description) {
        this.description = markDirty("description", description);
    }

    public void setCountry(Country country) {
        this.country = markDirty("country", country);
    }

    public void setIdentity(Identity identity) {
        this.identity = markDirty("identity", identity);
    }

    public void setProofs(List<Proof> proofs) {
        this.proofs = markDirty("proofs", proofs);
    }

    public void setArea(Area area) {
        this.area = markDirty("area", area);
    }

    public void setCity(City city) {
        this.city = markDirty("city", city);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }
}
