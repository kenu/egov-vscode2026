package com.example.asyncmessaging;

import java.time.Duration;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class InventoryConsumer {

    private static final String NAME = "InventoryConsumer";

    private final ProcessedMessageStore processedMessageStore;

    InventoryConsumer(ProcessedMessageStore processedMessageStore) {
        this.processedMessageStore = processedMessageStore;
    }

    @EventListener
    void on(OrderCreatedEvent event) {
        if (!processedMessageStore.markIfFirst(NAME, event.eventId())) {
            DemoLog.focus(NAME + " 중복 메시지 스킵: 같은 메시지가 두 번 와도 한 번만 처리합니다. eventId=" + event.eventId());
            return;
        }

        DemoLog.sleep(NAME + " 재고 차감 처리: productId=" + event.productId() + ", quantity=" + event.quantity(), Duration.ofMillis(600));
        DemoLog.info(NAME + " 처리 완료");
    }
}
