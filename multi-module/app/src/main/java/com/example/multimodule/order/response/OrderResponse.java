package com.example.multimodule.order.response;

public record OrderResponse(

        String message
) {
    public static OrderResponse create(String message){
        return new OrderResponse(message);
    }
}
