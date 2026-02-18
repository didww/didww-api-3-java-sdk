package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.util.List;

@Type("capacity_pools")
public class CapacityPool implements HasId {

    @Id
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("renew_date")
    private String renewDate;

    @JsonProperty("total_channels_count")
    private Integer totalChannelsCount;

    @JsonProperty("assigned_channels_count")
    private Integer assignedChannelsCount;

    @JsonProperty("minimum_limit")
    private Integer minimumLimit;

    @JsonProperty("minimum_qty_per_order")
    private Integer minimumQtyPerOrder;

    @JsonProperty("setup_price")
    private Double setupPrice;

    @JsonProperty("monthly_price")
    private Double monthlyPrice;

    @JsonProperty("metered_rate")
    private Double meteredRate;

    @Relationship("countries")
    private List<Country> countries;

    @Relationship("shared_capacity_groups")
    private List<SharedCapacityGroup> sharedCapacityGroups;

    @Relationship("qty_based_pricings")
    private List<QtyBasedPricing> qtyBasedPricings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getRenewDate() {
        return renewDate;
    }

    public Integer getTotalChannelsCount() {
        return totalChannelsCount;
    }

    public void setTotalChannelsCount(Integer totalChannelsCount) {
        this.totalChannelsCount = totalChannelsCount;
    }

    public Integer getAssignedChannelsCount() {
        return assignedChannelsCount;
    }

    public Integer getMinimumLimit() {
        return minimumLimit;
    }

    public Integer getMinimumQtyPerOrder() {
        return minimumQtyPerOrder;
    }

    public Double getSetupPrice() {
        return setupPrice;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public Double getMeteredRate() {
        return meteredRate;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public List<SharedCapacityGroup> getSharedCapacityGroups() {
        return sharedCapacityGroups;
    }

    public List<QtyBasedPricing> getQtyBasedPricings() {
        return qtyBasedPricings;
    }
}
