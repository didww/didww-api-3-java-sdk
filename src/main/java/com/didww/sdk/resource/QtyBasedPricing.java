package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("qty_based_pricings")
public class QtyBasedPricing {

    @Id
    private String id;

    @JsonProperty("qty")
    private Integer qty;

    @JsonProperty("setup_price")
    private Double setupPrice;

    @JsonProperty("monthly_price")
    private Double monthlyPrice;

    public String getId() {
        return id;
    }

    public Integer getQty() {
        return qty;
    }

    public Double getSetupPrice() {
        return setupPrice;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }
}
