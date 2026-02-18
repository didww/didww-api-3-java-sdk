package com.didww.sdk.converter;

import com.didww.sdk.resource.orderitem.OrderItem;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDeserializer extends JsonDeserializer<List<OrderItem>> {

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
                items.add(PolymorphicJsonHelper.deserialize(mapper, itemNode,
                        OrderItem.getTypeMap(), "order item"));
            }
        }
        return items;
    }
}
