package com.didww.sdk.resource;

import com.didww.sdk.converter.OrderItemDeserializer;
import com.didww.sdk.converter.OrderItemSerializer;
import com.didww.sdk.resource.orderitem.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Type("orders")
public class Order implements HasId {

    @Id
    private String id;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("allow_back_ordering")
    private Boolean allowBackOrdering;

    @JsonProperty("items")
    @JsonDeserialize(using = OrderItemDeserializer.class)
    @JsonSerialize(using = OrderItemSerializer.class)
    private List<OrderItem> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public String getStatus() {
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
