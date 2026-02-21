package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.CallbackMethod;
import com.didww.sdk.resource.enums.OrderStatus;
import com.didww.sdk.converter.OrderItemDeserializer;
import com.didww.sdk.converter.OrderItemSerializer;
import com.didww.sdk.resource.orderitem.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Type("orders")
public class Order extends BaseResource {

    public static Order build(String id) {
        return BaseResource.build(Order.class, id);
    }

    @JsonProperty(value = "amount", access = JsonProperty.Access.WRITE_ONLY)
    private Double amount;

    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private OrderStatus status;

    @JsonProperty(value = "description", access = JsonProperty.Access.WRITE_ONLY)
    private String description;

    @JsonProperty(value = "reference", access = JsonProperty.Access.WRITE_ONLY)
    private String reference;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("callback_method")
    private CallbackMethod callbackMethod;

    @JsonProperty("allow_back_ordering")
    private Boolean allowBackOrdering;

    @JsonProperty("items")
    @JsonDeserialize(using = OrderItemDeserializer.class)
    @JsonSerialize(using = OrderItemSerializer.class)
    private List<OrderItem> items;

    public Double getAmount() {
        return amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public CallbackMethod getCallbackMethod() {
        return callbackMethod;
    }

    public void setCallbackMethod(CallbackMethod callbackMethod) {
        this.callbackMethod = callbackMethod;
    }

    public Boolean getAllowBackOrdering() {
        return allowBackOrdering;
    }

    public void setAllowBackOrdering(Boolean allowBackOrdering) {
        this.allowBackOrdering = allowBackOrdering;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
