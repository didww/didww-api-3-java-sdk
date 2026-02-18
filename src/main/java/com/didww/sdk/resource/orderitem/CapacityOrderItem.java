package com.didww.sdk.resource.orderitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CapacityOrderItem extends OrderItem {

    @JsonProperty("capacity_pool_id")
    private String capacityPoolId;

    @JsonProperty("qty")
    private Integer qty;

    @Override
    @JsonIgnore
    public String getType() {
        return "capacity_order_items";
    }

    public String getCapacityPoolId() {
        return capacityPoolId;
    }

    public void setCapacityPoolId(String capacityPoolId) {
        this.capacityPoolId = capacityPoolId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
}
