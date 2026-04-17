package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.Feature;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.util.List;

@Type("did_groups")

@Getter
public class DidGroup extends BaseResource {

    @JsonProperty("area_name")
    private String areaName;

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("features")
    private List<Feature> features;

    @JsonProperty("is_metered")
    private Boolean isMetered;

    @JsonProperty("allow_additional_channels")
    private Boolean allowAdditionalChannels;

    @JsonProperty("service_restrictions")
    private String serviceRestrictions;

    @Relationship("stock_keeping_units")
    private List<StockKeepingUnit> stockKeepingUnits;

    @Relationship("country")
    private Country country;

    @Relationship("region")
    private Region region;

    @Relationship("city")
    private City city;

    @Relationship("did_group_type")
    private DidGroupType didGroupType;

    @Relationship("address_requirement")
    private AddressRequirement addressRequirement;
}
