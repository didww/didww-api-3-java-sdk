package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.AreaLevel;
import com.didww.sdk.resource.enums.IdentityType;
import com.didww.sdk.resource.enums.NumberType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.util.List;

@Type("address_requirements")

@Getter
public class AddressRequirement extends BaseResource {

    @JsonProperty("identity_type")
    private IdentityType identityType;

    @JsonProperty("personal_area_level")
    private AreaLevel personalAreaLevel;

    @JsonProperty("business_area_level")
    private AreaLevel businessAreaLevel;

    @JsonProperty("address_area_level")
    private AreaLevel addressAreaLevel;

    @JsonProperty("personal_proof_qty")
    private Integer personalProofQty;

    @JsonProperty("business_proof_qty")
    private Integer businessProofQty;

    @JsonProperty("address_proof_qty")
    private Integer addressProofQty;

    /**
     * Identity fields that must be filled in for a personal identity, among {@code birth_date},
     * {@code country}, {@code birth_country}, {@code id_number}, {@code personal_tax_id} and
     * {@code contact_email}. Empty when none are.
     */
    @JsonProperty("personal_mandatory_fields")
    private List<String> personalMandatoryFields;

    /**
     * Identity fields that must be filled in for a business identity, among {@code id_number},
     * {@code tax_id} (the {@code vat_id} of an {@link Identity}), {@code country}, {@code company_reg_number},
     * {@code personal_tax_id} and {@code contact_email}. Empty when none are.
     */
    @JsonProperty("business_mandatory_fields")
    private List<String> businessMandatoryFields;

    @JsonProperty("service_description_required")
    private Boolean serviceDescriptionRequired;

    @JsonProperty("restriction_message")
    private String restrictionMessage;

    @JsonProperty("number_type")
    private NumberType numberType;

    @Relationship("country")
    private Country country;

    @Relationship("personal_permanent_document")
    private SupportingDocumentTemplate personalPermanentDocument;

    @Relationship("business_permanent_document")
    private SupportingDocumentTemplate businessPermanentDocument;

    @Relationship("personal_onetime_document")
    private SupportingDocumentTemplate personalOnetimeDocument;

    @Relationship("business_onetime_document")
    private SupportingDocumentTemplate businessOnetimeDocument;

    @Relationship("personal_proof_types")
    private List<ProofType> personalProofTypes;

    @Relationship("business_proof_types")
    private List<ProofType> businessProofTypes;

    @Relationship("address_proof_types")
    private List<ProofType> addressProofTypes;
}
