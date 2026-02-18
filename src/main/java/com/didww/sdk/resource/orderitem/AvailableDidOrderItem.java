package com.didww.sdk.resource.orderitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AvailableDidOrderItem extends DidOrderItem {

    @JsonProperty("available_did_id")
    private String availableDidId;

    @Override
    @JsonIgnore
    public String getType() {
        return "available_did_order_items";
    }

    public String getAvailableDidId() {
        return availableDidId;
    }

    public void setAvailableDidId(String availableDidId) {
        this.availableDidId = availableDidId;
    }
}
