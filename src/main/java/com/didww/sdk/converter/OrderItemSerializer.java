package com.didww.sdk.converter;

import com.didww.sdk.resource.orderitem.OrderItem;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

public class OrderItemSerializer extends JsonSerializer<List<OrderItem>> {

    @Override
    public void serialize(List<OrderItem> value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        ObjectMapper mapper = (ObjectMapper) gen.getCodec();
        gen.writeStartArray();
        for (OrderItem item : value) {
            ObjectNode wrapper = mapper.createObjectNode();
            wrapper.put("type", item.getType());
            wrapper.set("attributes", mapper.valueToTree(item));
            gen.writeTree(wrapper);
        }
        gen.writeEndArray();
    }
}
