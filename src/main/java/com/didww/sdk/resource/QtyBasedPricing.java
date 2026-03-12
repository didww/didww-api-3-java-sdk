package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("qty_based_pricings")

@Getter
public class QtyBasedPricing extends BaseResource {

    @JsonProperty("qty")
    private Integer qty;

    @JsonProperty("setup_price")
    private Double setupPrice;

    @JsonProperty("monthly_price")
    private Double monthlyPrice;
}
