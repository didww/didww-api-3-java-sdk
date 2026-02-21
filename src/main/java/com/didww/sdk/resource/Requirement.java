package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.AreaLevel;
import com.didww.sdk.resource.enums.IdentityType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.util.List;

@Type("requirements")
public class Requirement extends BaseResource {

    public static Requirement build(String id) {
        return BaseResource.build(Requirement.class, id);
    }

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

    @JsonProperty("personal_mandatory_fields")
    private List<String> personalMandatoryFields;

    @JsonProperty("business_mandatory_fields")
    private List<String> businessMandatoryFields;

    @JsonProperty("service_description_required")
    private Boolean serviceDescriptionRequired;

    @JsonProperty("restriction_message")
    private String restrictionMessage;

    @Relationship("country")
    private Country country;

    @Relationship("did_group_type")
    private DidGroupType didGroupType;

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

    public IdentityType getIdentityType() {
        return identityType;
    }

    public AreaLevel getPersonalAreaLevel() {
        return personalAreaLevel;
    }

    public AreaLevel getBusinessAreaLevel() {
        return businessAreaLevel;
    }

    public AreaLevel getAddressAreaLevel() {
        return addressAreaLevel;
    }

    public Integer getPersonalProofQty() {
        return personalProofQty;
    }

    public Integer getBusinessProofQty() {
        return businessProofQty;
    }

    public Integer getAddressProofQty() {
        return addressProofQty;
    }

    public List<String> getPersonalMandatoryFields() {
        return personalMandatoryFields;
    }

    public List<String> getBusinessMandatoryFields() {
        return businessMandatoryFields;
    }

    public Boolean getServiceDescriptionRequired() {
        return serviceDescriptionRequired;
    }

    public String getRestrictionMessage() {
        return restrictionMessage;
    }

    public Country getCountry() {
        return country;
    }

    public DidGroupType getDidGroupType() {
        return didGroupType;
    }

    public SupportingDocumentTemplate getPersonalPermanentDocument() {
        return personalPermanentDocument;
    }

    public SupportingDocumentTemplate getBusinessPermanentDocument() {
        return businessPermanentDocument;
    }

    public SupportingDocumentTemplate getPersonalOnetimeDocument() {
        return personalOnetimeDocument;
    }

    public SupportingDocumentTemplate getBusinessOnetimeDocument() {
        return businessOnetimeDocument;
    }

    public List<ProofType> getPersonalProofTypes() {
        return personalProofTypes;
    }

    public List<ProofType> getBusinessProofTypes() {
        return businessProofTypes;
    }

    public List<ProofType> getAddressProofTypes() {
        return addressProofTypes;
    }
}
