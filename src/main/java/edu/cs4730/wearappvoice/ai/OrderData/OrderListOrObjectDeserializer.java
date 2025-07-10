package edu.cs4730.wearappvoice.ai.OrderData;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderListOrObjectDeserializer extends JsonDeserializer<List<Order>> {

    @Override
    public List<Order> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);
        List<Order> orders = new ArrayList<>();

        if (node.isArray()) {
            for (JsonNode element : node) {
                orders.add(codec.treeToValue(element, Order.class));
            }
        } else if (node.isObject()) {
            orders.add(codec.treeToValue(node, Order.class));
        }

        return orders;
    }
}
