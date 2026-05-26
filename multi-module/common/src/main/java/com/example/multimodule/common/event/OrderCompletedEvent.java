package com.example.multimodule.common.event;

public record OrderCompletedEvent(
        String userId,
        int purchaseAmount
) {

}