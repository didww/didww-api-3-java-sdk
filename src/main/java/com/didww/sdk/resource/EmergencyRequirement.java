package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Meta;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Requirements that must be satisfied before ordering an emergency
 * calling service for a given country/did_group_type. Introduced in API 2026-04-16.
 *
 * <p>Each resource includes server-provided {@code meta} with pricing information:
 * <ul>
 *   <li>{@code setup_price} - one-time setup fee (as a decimal string, e.g. "10.0")</li>
 *   <li>{@code monthly_price} - recurring monthly fee (as a decimal string, e.g. "2.5")</li>
 * </ul>
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

    /**
     * Server-provided meta containing pricing information.
     * Keys: "setup_price", "monthly_price".
     */
    @Meta
    private Map<String, String> meta;

    /**
     * Returns the one-time setup price from the resource meta.
     *
     * @return the "setup_price" meta value, or null if not present
     */
    @JsonIgnore
    public String getMetaSetupPrice() {
        return meta != null ? meta.get("setup_price") : null;
    }

    /**
     * Returns the recurring monthly price from the resource meta.
     *
     * @return the "monthly_price" meta value, or null if not present
     */
    @JsonIgnore
    public String getMetaMonthlyPrice() {
        return meta != null ? meta.get("monthly_price") : null;
    }
}
