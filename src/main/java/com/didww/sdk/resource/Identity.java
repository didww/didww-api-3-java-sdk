package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Type("identities")
public class Identity extends BaseResource {

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
    private String identityType;

    @JsonProperty("external_reference_id")
    private String externalReferenceId;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyRegNumber() {
        return companyRegNumber;
    }

    public void setCompanyRegNumber(String companyRegNumber) {
        this.companyRegNumber = companyRegNumber;
    }

    public String getVatId() {
        return vatId;
    }

    public void setVatId(String vatId) {
        this.vatId = vatId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPersonalTaxId() {
        return personalTaxId;
    }

    public void setPersonalTaxId(String personalTaxId) {
        this.personalTaxId = personalTaxId;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getVerified() {
        return verified;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<Proof> getProofs() {
        return proofs;
    }

    public void setProofs(List<Proof> proofs) {
        this.proofs = proofs;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<PermanentSupportingDocument> getPermanentDocuments() {
        return permanentDocuments;
    }

    public void setPermanentDocuments(List<PermanentSupportingDocument> permanentDocuments) {
        this.permanentDocuments = permanentDocuments;
    }
}
