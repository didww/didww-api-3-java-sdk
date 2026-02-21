package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.LocalDate;
import java.util.List;

@Type("capacity_pools")
public class CapacityPool extends BaseResource {

    public static CapacityPool build(String id) {
        return BaseResource.build(CapacityPool.class, id);
    }

    @JsonProperty(value = "name", access = JsonProperty.Access.WRITE_ONLY)
    private String name;

    @JsonProperty(value = "renew_date", access = JsonProperty.Access.WRITE_ONLY)
    private LocalDate renewDate;

    @JsonProperty("total_channels_count")
    private Integer totalChannelsCount;

    @JsonProperty(value = "assigned_channels_count", access = JsonProperty.Access.WRITE_ONLY)
    private Integer assignedChannelsCount;

    @JsonProperty(value = "minimum_limit", access = JsonProperty.Access.WRITE_ONLY)
    private Integer minimumLimit;

    @JsonProperty(value = "minimum_qty_per_order", access = JsonProperty.Access.WRITE_ONLY)
    private Integer minimumQtyPerOrder;

    @JsonProperty(value = "setup_price", access = JsonProperty.Access.WRITE_ONLY)
    private Double setupPrice;

    @JsonProperty(value = "monthly_price", access = JsonProperty.Access.WRITE_ONLY)
    private Double monthlyPrice;

    @JsonProperty(value = "metered_rate", access = JsonProperty.Access.WRITE_ONLY)
    private Double meteredRate;

    @Relationship("countries")
    private List<Country> countries;

    @Relationship("shared_capacity_groups")
    private List<SharedCapacityGroup> sharedCapacityGroups;

    @Relationship("qty_based_pricings")
    private List<QtyBasedPricing> qtyBasedPricings;

    public String getName() {
        return name;
    }

    public LocalDate getRenewDate() {
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
