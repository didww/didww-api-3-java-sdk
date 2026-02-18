package com.didww.sdk.resource.orderitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GenericOrderItem extends OrderItem {

    @JsonProperty("qty")
    private Integer qty;

    @Override
    @JsonIgnore
    public String getType() {
        return "generic_order_items";
    }

    public Integer getQty() {
        return qty;
    }
}
