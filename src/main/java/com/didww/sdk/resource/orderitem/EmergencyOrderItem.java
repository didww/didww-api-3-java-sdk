package com.didww.sdk.resource.orderitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Order item for emergency calling service subscriptions.
 * Introduced in API 2026-04-16.
 */
public class EmergencyOrderItem extends OrderItem {

    @JsonProperty("qty")
    private Integer qty;

    @JsonProperty("emergency_calling_service_id")
    private String emergencyCallingServiceId;

    @JsonProperty("nrc")
    private String nrc;

    @JsonProperty("mrc")
    private String mrc;

    @JsonProperty("prorated_mrc")
    private Boolean proratedMrc;

    @JsonProperty("billed_from")
    private String billedFrom;

    @JsonProperty("billed_to")
    private String billedTo;

    @Override
    @JsonIgnore
    public String getType() {
        return "emergency_order_items";
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getEmergencyCallingServiceId() {
        return emergencyCallingServiceId;
    }

    public void setEmergencyCallingServiceId(String emergencyCallingServiceId) {
        this.emergencyCallingServiceId = emergencyCallingServiceId;
    }

    public String getNrc() {
        return nrc;
    }

    public String getMrc() {
        return mrc;
    }

    public Boolean getProratedMrc() {
        return proratedMrc;
    }

    public String getBilledFrom() {
        return billedFrom;
    }

    public String getBilledTo() {
        return billedTo;
    }
}
