package com.didww.sdk.resource.orderitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DidOrderItem extends OrderItem {

    @JsonProperty("sku_id")
    private String skuId;

    @JsonProperty("qty")
    private Integer qty;

    @JsonProperty("nrc")
    private Double nrc;

    @JsonProperty("mrc")
    private Double mrc;

    @JsonProperty("billed_from")
    private String billedFrom;

    @JsonProperty("billed_to")
    private String billedTo;

    @JsonProperty("prorated_mrc")
    private Boolean proratedMrc;

    @JsonProperty("nanpa_prefix_id")
    private String nanpaPrefixId;

    @JsonProperty("billing_cycles_count")
    private Integer billingCyclesCount;

    @JsonProperty("did_group_id")
    private String didGroupId;

    @Override
    @JsonIgnore
    public String getType() {
        return "did_order_items";
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Double getNrc() {
        return nrc;
    }

    public Double getMrc() {
        return mrc;
    }

    public String getBilledFrom() {
        return billedFrom;
    }

    public String getBilledTo() {
        return billedTo;
    }

    public Boolean getProratedMrc() {
        return proratedMrc;
    }

    public String getNanpaPrefixId() {
        return nanpaPrefixId;
    }

    public void setNanpaPrefixId(String nanpaPrefixId) {
        this.nanpaPrefixId = nanpaPrefixId;
    }

    public Integer getBillingCyclesCount() {
        return billingCyclesCount;
    }

    public void setBillingCyclesCount(Integer billingCyclesCount) {
        this.billingCyclesCount = billingCyclesCount;
    }

    public String getDidGroupId() {
        return didGroupId;
    }

    public void setDidGroupId(String didGroupId) {
        this.didGroupId = didGroupId;
    }
}
