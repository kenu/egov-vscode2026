package com.example.asyncmessaging;

import java.math.BigDecimal;
import java.time.Instant;

record OrderCreatedEvent(
        String eventId,
        String orderId,
        String productId,
        int quantity,
        BigDecimal amount,
        boolean notificationAlwaysFails,
        Instant occurredAt
) {
}
