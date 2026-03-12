package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("stock_keeping_units")

@Getter
public class StockKeepingUnit extends BaseResource {

    @JsonProperty("setup_price")
    private Double setupPrice;

    @JsonProperty("monthly_price")
    private Double monthlyPrice;

    @JsonProperty("channels_included_count")
    private Integer channelsIncludedCount;
}
