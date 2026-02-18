package com.didww.sdk.converter;

import com.didww.sdk.resource.orderitem.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderItemDeserializer extends JsonDeserializer<List<OrderItem>> {

    private static final Map<String, Class<? extends OrderItem>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("did_order_items", DidOrderItem.class);
        TYPE_MAP.put("available_did_order_items", AvailableDidOrderItem.class);
        TYPE_MAP.put("reservation_did_order_items", ReservationDidOrderItem.class);
        TYPE_MAP.put("capacity_order_items", CapacityOrderItem.class);
        TYPE_MAP.put("generic_order_items", GenericOrderItem.class);
    }

    @Override
    public List<OrderItem> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }

        List<OrderItem> items = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode itemNode : node) {
                items.add(deserializeItem(mapper, itemNode));
            }
        }
        return items;
    }

    private OrderItem deserializeItem(ObjectMapper mapper, JsonNode node) throws IOException {
        String type = node.has("type") ? node.get("type").asText() : null;
        if (type == null) {
            throw new IOException("Missing 'type' field in order item");
        }

        Class<? extends OrderItem> clazz = TYPE_MAP.get(type);
        if (clazz == null) {
            throw new IOException("Unknown order item type: " + type);
        }

        JsonNode attributes = node.get("attributes");
        if (attributes == null) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IOException("Cannot instantiate " + clazz.getName(), e);
            }
        }

        return mapper.treeToValue(attributes, clazz);
    }
}
