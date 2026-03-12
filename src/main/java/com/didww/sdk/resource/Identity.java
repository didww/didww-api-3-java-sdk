package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.IdentityType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Type("identities")

@Getter
public class Identity extends BaseResource implements ProofEntity {

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("id_number")
    private String idNumber;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_reg_number")
    private String companyRegNumber;

    @JsonProperty("vat_id")
    private String vatId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("personal_tax_id")
    private String personalTaxId;

    @JsonProperty("identity_type")
    private IdentityType identityType;

    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty(value = "verified", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean verified;

    @Relationship("country")
    private Country country;

    @Relationship("proofs")
    private List<Proof> proofs;

    @Relationship("addresses")
    private List<Address> addresses;

    @Relationship("permanent_documents")
    private List<PermanentSupportingDocument> permanentDocuments;

    public void setFirstName(String firstName) {
        this.firstName = markDirty("firstName", firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = markDirty("lastName", lastName);
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = markDirty("phoneNumber", phoneNumber);
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = markDirty("idNumber", idNumber);
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = markDirty("birthDate", birthDate);
    }

    public void setCompanyName(String companyName) {
        this.companyName = markDirty("companyName", companyName);
    }

    public void setCompanyRegNumber(String companyRegNumber) {
        this.companyRegNumber = markDirty("companyRegNumber", companyRegNumber);
    }

    public void setVatId(String vatId) {
        this.vatId = markDirty("vatId", vatId);
    }

    public void setDescription(String description) {
        this.description = markDirty("description", description);
    }

    public void setPersonalTaxId(String personalTaxId) {
        this.personalTaxId = markDirty("personalTaxId", personalTaxId);
    }

    public void setIdentityType(IdentityType identityType) {
        this.identityType = markDirty("identityType", identityType);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = markDirty("contactEmail", contactEmail);
    }

    public void setCountry(Country country) {
        this.country = markDirty("country", country);
    }

    public void setProofs(List<Proof> proofs) {
        this.proofs = markDirty("proofs", proofs);
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = markDirty("addresses", addresses);
    }

    public void setPermanentDocuments(List<PermanentSupportingDocument> permanentDocuments) {
        this.permanentDocuments = markDirty("permanentDocuments", permanentDocuments);
    }
}
