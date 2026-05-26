package com.example.multimodule.order.request;

public record OrderRequest(

        String userId,
        String productName,
        int count
) {
}
