package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.util.List;

/**
 * Requirements that must be satisfied before ordering an emergency
 * calling service for a given country/did_group_type. Introduced in API 2026-04-16.
 */
@Type("emergency_requirements")
@Getter
public class EmergencyRequirement extends BaseResource {

    @JsonProperty("identity_type")
    private String identityType;

    @JsonProperty("address_area_level")
    private String addressAreaLevel;

    @JsonProperty("personal_area_level")
    private String personalAreaLevel;

    @JsonProperty("business_area_level")
    private String businessAreaLevel;

    @JsonProperty("address_mandatory_fields")
    private List<String> addressMandatoryFields;

    @JsonProperty("personal_mandatory_fields")
    private List<String> personalMandatoryFields;

    @JsonProperty("business_mandatory_fields")
    private List<String> businessMandatoryFields;

    @JsonProperty("estimate_setup_time")
    private String estimateSetupTime;

    @JsonProperty("requirement_restriction_message")
    private String requirementRestrictionMessage;

    @Relationship("country")
    private Country country;

    @Relationship("did_group_type")
    private DidGroupType didGroupType;
}
