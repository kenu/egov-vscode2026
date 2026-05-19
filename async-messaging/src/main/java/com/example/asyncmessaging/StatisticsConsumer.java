package com.example.asyncmessaging;

import java.time.Duration;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class StatisticsConsumer {

    private static final String NAME = "StatisticsConsumer";

    private final ProcessedMessageStore processedMessageStore;

    StatisticsConsumer(ProcessedMessageStore processedMessageStore) {
        this.processedMessageStore = processedMessageStore;
    }

    @EventListener
    void on(OrderCreatedEvent event) {
        if (!processedMessageStore.markIfFirst(NAME, event.eventId())) {
            DemoLog.focus(NAME + " 중복 메시지 스킵: 같은 메시지가 두 번 와도 한 번만 처리합니다. eventId=" + event.eventId());
            return;
        }

        DemoLog.sleep(NAME + " 주문 통계 집계: orderId=" + event.orderId(), Duration.ofMillis(350));
        DemoLog.info(NAME + " 처리 완료");
    }
}
