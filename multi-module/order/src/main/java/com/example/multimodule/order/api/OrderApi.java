package com.example.multimodule.order.api;

public interface OrderApi {

    void orderWithPort(String userId, String productName, int count);

    void orderWithEvent(String userId, String productName, int count);
}
