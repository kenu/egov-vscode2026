package com.example.asyncmessaging;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
class DeadLetterQueue {

    private final List<DeadLetterMessage> messages = new CopyOnWriteArrayList<>();

    void add(String consumerName, OrderCreatedEvent event, String reason) {
        messages.add(new DeadLetterMessage(consumerName, event.eventId(), event.orderId(), reason, Instant.now()));
        DemoLog.focus("DLQ 저장: 계속 실패한 메시지를 실패 메시지 보관함에 남깁니다. consumer=" + consumerName + ", orderId=" + event.orderId() + ", reason=" + reason);
    }

    void printAll() {
        if (messages.isEmpty()) {
            DemoLog.info("DLQ 비어 있음: 재처리할 실패 메시지가 없습니다.");
            return;
        }

        DemoLog.focus("DLQ 메시지 수=" + messages.size());
        for (DeadLetterMessage message : messages) {
            DemoLog.focus("DLQ 메시지: " + message);
        }
    }

    private record DeadLetterMessage(
            String consumerName,
            String eventId,
            String orderId,
            String reason,
            Instant storedAt
    ) {
    }
}
