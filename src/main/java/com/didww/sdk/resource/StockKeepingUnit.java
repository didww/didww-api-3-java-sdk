package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("stock_keeping_units")
public class StockKeepingUnit {

    @Id
    private String id;

    @JsonProperty("setup_price")
    private Double setupPrice;

    @JsonProperty("monthly_price")
    private Double monthlyPrice;

    @JsonProperty("channels_included_count")
    private Integer channelsIncludedCount;

    public String getId() {
        return id;
    }

    public Double getSetupPrice() {
        return setupPrice;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public Integer getChannelsIncludedCount() {
        return channelsIncludedCount;
    }
}
