package com.didww.sdk.resource.orderitem;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class OrderItem {

    private static final Map<String, Class<? extends OrderItem>> TYPE_MAP;

    static {
        Map<String, Class<? extends OrderItem>> map = new HashMap<>();
        map.put("did_order_items", DidOrderItem.class);
        map.put("capacity_order_items", CapacityOrderItem.class);
        map.put("generic_order_items", GenericOrderItem.class);
        TYPE_MAP = Collections.unmodifiableMap(map);
    }

    @JsonIgnore
    public abstract String getType();

    public static Map<String, Class<? extends OrderItem>> getTypeMap() {
        return TYPE_MAP;
    }
}
