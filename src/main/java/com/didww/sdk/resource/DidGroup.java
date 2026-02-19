package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.util.List;

@Type("did_groups")
public class DidGroup extends BaseResource {

    @JsonProperty("area_name")
    private String areaName;

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("features")
    private List<String> features;

    @JsonProperty("is_metered")
    private Boolean isMetered;

    @JsonProperty("allow_additional_channels")
    private Boolean allowAdditionalChannels;

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

    @Relationship("requirement")
    private Requirement requirement;

    public String getAreaName() {
        return areaName;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<String> getFeatures() {
        return features;
    }

    public Boolean getIsMetered() {
        return isMetered;
    }

    public Boolean getAllowAdditionalChannels() {
        return allowAdditionalChannels;
    }

    public List<StockKeepingUnit> getStockKeepingUnits() {
        return stockKeepingUnits;
    }

    public Country getCountry() {
        return country;
    }

    public Region getRegion() {
        return region;
    }

    public City getCity() {
        return city;
    }

    public DidGroupType getDidGroupType() {
        return didGroupType;
    }

    public Requirement getRequirement() {
        return requirement;
    }
}
