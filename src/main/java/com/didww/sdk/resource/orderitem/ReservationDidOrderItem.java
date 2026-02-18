package com.didww.sdk.resource.orderitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReservationDidOrderItem extends DidOrderItem {

    @JsonProperty("did_reservation_id")
    private String didReservationId;

    @Override
    @JsonIgnore
    public String getType() {
        return "reservation_did_order_items";
    }

    public String getDidReservationId() {
        return didReservationId;
    }

    public void setDidReservationId(String didReservationId) {
        this.didReservationId = didReservationId;
    }
}
