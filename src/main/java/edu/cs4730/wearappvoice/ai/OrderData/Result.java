package edu.cs4730.wearappvoice.ai.OrderData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    public String msg;

    @JsonDeserialize(using = OrderListOrObjectDeserializer.class)
    public List<Order> data = Collections.emptyList();  // 避免空指针

    public int code;
}
